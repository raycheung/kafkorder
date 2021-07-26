(ns kafkorder.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[kafkorder started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[kafkorder has shut down successfully]=-"))})
