(ns repl-wallet.impl.bitcoinj
  (:require [repl-wallet.protocol :refer :all])
  (:import [org.bitcoinj.core Wallet Coin BlockChain PeerGroup Address]
           [org.bitcoinj.store SPVBlockStore]
           [org.bitcoinj.params MainNetParams TestNet3Params]))

(extend-type Coin
  Satoshis
  (current-balance [coin]
    (.getValue coin)))

(defn- wallet->peergroup [^Wallet wallet]
  (let [params (.getNetworkParameters wallet)
        store (SPVBlockStore. params (java.io.File. ".bitcoin"))
        chain (BlockChain. params wallet store)]
    (PeerGroup. params chain)))

(extend-type Wallet
  Send
  (send [wallet amount ^String address]
    (let [params (.getNetworkParameters wallet)
          btcj-address (Address. params address)
          coin (Coin/valueOf amount)
          peers (wallet->peergroup wallet)]
      (.sendCoins wallet peers btcj-address coin)))
  Satoshis
  (current-balance [wallet]
    (-> wallet
        (.getBalance)
        (.getValue)))
  Addresses
  (addresses [wallet]
    (->> wallet
         (.getWatchedAddresses)
         (map str)))
  (new-address! [_]))

(defn ^Wallet make-wallet
  ([] (make-wallet :test))
  ([params]
     (let [wallet (Wallet. (case params
                             :test (TestNet3Params/get)
                             :main (MainNetParams/get)))
           peers (wallet->peergroup wallet)]
       (doto peers
         (.addWallet wallet)
         (.start))
       wallet)))
