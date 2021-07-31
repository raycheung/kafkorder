(ns kafkorder.env
  (:require
   [selmer.parser :as parser]
   [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[kafkorder started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[kafkorder has shut down successfully]=-"))})
