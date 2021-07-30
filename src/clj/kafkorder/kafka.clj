(ns kafkorder.kafka
  (:require
   [kafkorder.config :refer [env]]
   [mount.core :as mount]
   [kinsky.client :as k]
   [cheshire.core :as json]))

(mount/defstate consumer
  :start (k/consumer (env :kafka/config)
                     (k/string-deserializer)
                     (k/string-deserializer))
  :stop (k/close! consumer))

(mount/defstate producer
  :start (k/producer (env :kafka/config)
                     (k/string-serializer)
                     (k/string-serializer))
  :stop (k/close! producer))

(defn dump [& {:keys [topic to read-from-beginning?]}]
  (let [assigned? (promise)
        seek-on-assign (fn [{:keys [event partitions]}]
                         (when (= :assigned event)
                           (deliver assigned? true)
                           (if read-from-beginning?
                             (doseq [tp partitions]
                               (k/seek! consumer tp 0)))))]
    (k/subscribe! consumer topic seek-on-assign)
    (loop []
      (let [{{msgs topic} :by-topic} (k/poll! consumer 200)]
        (when (and @assigned? (not-empty msgs))
          (doseq [msg msgs]
            (.write to (-> msg
                           (select-keys [:key :value])
                           (json/generate-string)
                           (str "\n"))))
          (recur))))
    (k/unsubscribe! consumer)))

(defn replay [& {:keys [topic from]}]
  (doseq [line (line-seq from)]
    (let [{:keys [key value]} (json/parse-string line true)]
      (k/send! producer topic key value))))
