(ns repl-wallet.impl.bitcoinj
  (:refer-clojure :exclude [send])
  (:require [repl-wallet.protocol :refer :all])
  (:import [java.io File]
           [org.bitcoinj.core Wallet Coin BlockChain PeerGroup Address]
           [org.bitcoinj.store SPVBlockStore]
           [org.bitcoinj.params MainNetParams TestNet3Params]))

(def base-dir
  (->> "/.bitcoin/repl-wallet"
       (str (System/getProperty "user.home"))
       (delay)))

(defn- ->file [file-name]
  (File. (str @base-dir "/bitcoinj/" file-name)))

(defn- wallet->spvchain [^Wallet wallet]
  (let [params (.getNetworkParameters wallet)
        store (SPVBlockStore. params (->file "spvstore"))]
    (BlockChain. params wallet store)))

(defrecord BitcoinJWallet [^Wallet wallet ^PeerGroup peergroup]
  Send
  (send [_ amount address]
    (let [params (.getNetworkParameters wallet)
          btcj-address (Address. params address)
          coin (Coin/valueOf amount)]
      (.sendCoins wallet peergroup btcj-address coin)))
  Satoshis
  (current-balance [_]
    (-> wallet
        (.getBalance)
        (.getValue)))
  Addresses
  (addresses [_]
    (->> wallet
         (.getIssuedReceiveAddresses)
         (map str)))
  (new-address! [_]
    (str (.freshReceiveAddress wallet)))
  Persist
  (save [_ file-name]
    (let [file (->file file-name)]
      (.mkdirs (.getParentFile file))
      (.saveToFile wallet file))))

(defn load-wallet* [file-name]
  (-> file-name
      (->file)
      (Wallet/loadFromFile nil)))

(defn new-wallet*
  ([] (new-wallet* :test))
  ([params]
     (Wallet. (case params
                :test (TestNet3Params/get)
                :main (MainNetParams/get)))))

(defn start-wallet! [^Wallet wallet]
  (let [blockchain (wallet->spvchain wallet)
        peers (PeerGroup. (.getNetworkParameters wallet) blockchain)]
    (.addWallet blockchain wallet)
    (doto peers
      (.addWallet wallet)
      (.start))
    [wallet peers]))

(def start-convert!
  (comp (partial apply ->BitcoinJWallet) start-wallet!))

(def load-wallet (comp start-convert! load-wallet*))

(def new-wallet (comp start-convert! new-wallet*))
