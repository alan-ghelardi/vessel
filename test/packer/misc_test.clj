(ns packer.misc-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [packer.misc :as misc])
  (:import java.io.StringWriter
           [java.time Duration Instant]
           java.util.function.Consumer))

(def kv-gen (gen/tuple gen/keyword (gen/such-that (complement nil?) gen/any)))

(defspec assoc-some-spec-test
  {:num-tests 25}
  (prop/for-all [kvs (gen/fmap (partial mapcat identity)
                               (gen/not-empty (gen/vector kv-gen)))]
                (testing "assoc-some behaves like assoc for non-nil values"
                  (is (= (apply assoc {} kvs)
                         (apply  misc/assoc-some {} kvs))))))

(deftest assoc-some-test
  (is (= {:a 1}
         (misc/assoc-some {}
                          :a 1 :b nil))))

(deftest java-consumer-test
  (is (instance? Consumer
                 (misc/java-consumer identity)))

  (is (= "Hello world!"
         (with-out-str
           (.. (misc/java-consumer #(printf %))
               (accept "Hello world!"))))))

(deftest now-test
  (is (instance? Instant (misc/now))))

(deftest duration-between-test
  (is (instance? Duration (misc/duration-between (misc/now)
                                                 (misc/now)))))

(deftest duration->string-test
  (are [duration result] (= result (misc/duration->string duration))
    (Duration/ZERO)   "0 milliseconds"
    (Duration/ofMillis 1)    "1 millisecond"
    (Duration/ofMillis 256) "256 milliseconds"
    (Duration/ofMillis 1000)         "1 second"
    (Duration/ofMillis 6537)     "6.54 seconds"
    (Duration/ofMinutes 1)         "1 minute"
    (Duration/ofMillis 63885)     "1.06 minutes"
    (Duration/ofMinutes 4)        "4 minutes"))

(deftest with-stderr-test
  (let [writer (StringWriter.)]
    (binding [*err* writer]
      (misc/with-stderr
        (print "Error!"))
      (is (= "Error!"
             (str writer))))))
(deftest sha-256-test
  (is (= "d2cf1a50c1a07db39d8397d4815da14aa7c7230775bb3c94ea62c9855cf9488d"
         (misc/sha-256 {:image
                        {:name "my-app"
                         :registry "docker.io"
                         :version "v1"}}))))
