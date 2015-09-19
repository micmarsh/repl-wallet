(ns repl-wallet.protocol
  (:refer-clojure :exclude [send]))

(defprotocol Satoshis
  (current-balance [_]
    "Returns a number in Satoshis representing the
     current balance of the object"))

(defprotocol Send
  (send [_ amount address]
    "Sends the given amount of Satoshis to
     the given address"))

(defprotocol Addresses
  (new-address! [_]
    "Generate a new Bitcoin address. Make sure this is implemented securely
     possible TODOs:
      * make this return a new instance of the record, state monad style
        (this will likely just be a separate protocol)
      * an optional \"entropy\" or \"seed\" argument")
  (addresses [_]
    "List all addresses (implementing Satoshis, above) associated with this wallet"))

(defprotocol Persist
  (save [_ path]
    "Saves the given item to the given file name"))
