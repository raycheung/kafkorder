(ns kafkorder.core
  (:require
   [kafkorder.action :as action]
   [clojure.tools.cli :refer [parse-opts]]
   [cprop.core :refer [load-config]]
   [cprop.source :refer [from-env]]
   [clojure.tools.logging :as log])
  (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/error {:what :uncaught-exception
                 :exception ex
                 :where (str "Uncaught exception on" (.getName thread))}))))

(def cli-options
  [["-d" "--dump" "DUMP mode"]
   ["-r" "--replay" "REPLAY mode"]
   ["-t" "--topic TOPIC"]
   ["-c" "--count NUM" "Number of messages to dump"
    :parse-fn #(Integer/parseInt %)]
   ["-f" "--file FILE" "File"]])

(defn -main [& args]
  (let [env (load-config
             :merge
             [(:options (parse-opts args cli-options))
              (from-env)])]
    (cond
      (:dump env)
      (action/dump (env :kafka) (env :topic) (env :file) (env :count))

      (:replay env)
      (action/replay (env :kafka) (env :topic) (env :file)))))
