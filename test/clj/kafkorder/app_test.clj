(ns kafkorder.handler-test
  (:require
   [clojure.test :refer :all]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'kafkorder.config/env)
    (f)))

(deftest test-app)
