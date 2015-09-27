(ns repl-wallet.impl.bitcoind
  (:refer-clojure :exclude [send])
  (:require [repl-wallet.protocol :as p]
            [clj-btc.core :as btc]))

(def ^:const satoshi-per-btc 100000000M)

(defrecord BitcoinDWallet [config]
  p/Send
  (send [_ amount address]
    (btc/sendfrom :fromaccount ""
                  :tobitcoinaddress address
                  :amount (double (/ amount satoshi-per-btc))))
  p/Satoshis
  (current-balance [_]
    (->> config
         (btc/getbalance :config)
         (* satoshi-per-btc)
         (int)))
  p/Addresses
  (addresses [_]
    (btc/getaddressesbyaccount :account ""))
  (new-address! [_]
    (btc/getnewaddress :account "")))
