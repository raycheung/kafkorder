(ns kafkorder.action
  (:require
   [kinsky.client :as k]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]))

(defn dump [kafka topic file n]
  (let [consumer (k/consumer kafka
                             (k/string-deserializer)
                             (k/string-deserializer))
        assigned? (promise)
        seek-on-assign #(when (= :assigned (:event %))
                          (deliver assigned? true))]
    (k/subscribe! consumer topic seek-on-assign)
    (loop [c n]
      (let [{{msgs topic} :by-topic} (k/poll! consumer 500)]
        (when (and @assigned? (if n (pos? c) (not-empty msgs)))
          (with-open [to (clojure.java.io/writer file :append true)]
            (doseq [msg (if n (take c msgs) msgs)]
              (.write to (-> msg
                             (select-keys [:key :value])
                             (json/generate-string)
                             (str "\n")))))
          (recur (if n (- c (min c (count msgs))))))))
    (k/unsubscribe! consumer)
    (k/close! consumer)))

(defn replay [kafka topic file]
  (let [producer (k/producer kafka
                             (k/string-serializer)
                             (k/string-serializer))]
    (with-open [from (clojure.java.io/reader file)]
      (doseq [line (line-seq from)]
        (let [{:keys [key value]} (json/parse-string line true)]
          (k/send! producer topic key value))))
    (k/close! producer)))
