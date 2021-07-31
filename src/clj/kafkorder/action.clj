(ns kafkorder.action
  (:require
   [kafkorder.config :refer [env]]
   [mount.core :as mount]
   [kinsky.client :as k]
   [cheshire.core :as json]))

(mount/defstate consumer
  :start (k/consumer (env :kafka)
                     (k/string-deserializer)
                     (k/string-deserializer))
  :stop (k/close! consumer))

(defn dump [& {:keys [topic to n]}]
  (let [assigned? (promise)
        seek-on-assign (fn [{:keys [event]}]
                         (if (= :assigned event)
                           (deliver assigned? true)))]
    (k/subscribe! consumer topic seek-on-assign)
    (loop [c n]
      (let [{{msgs topic} :by-topic} (k/poll! consumer 200)]
        (when (and @assigned? (pos? c) (not-empty msgs))
          (doseq [msg (take c msgs)]
            (.write to (-> msg
                           (select-keys [:key :value])
                           (json/generate-string)
                           (str "\n"))))
          (recur (- c (count msgs))))))
    (k/unsubscribe! consumer)))

(mount/defstate producer
  :start (k/producer (env :kafka)
                     (k/string-serializer)
                     (k/string-serializer))
  :stop (k/close! producer))

(defn replay [& {:keys [topic from]}]
  (doseq [line (line-seq from)]
    (let [{:keys [key value]} (json/parse-string line true)]
      (k/send! producer topic key value))))
