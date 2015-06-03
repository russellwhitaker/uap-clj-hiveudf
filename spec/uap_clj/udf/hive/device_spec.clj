(ns uap-clj.udf.hive.device-spec
  (:import [org.apache.hadoop.io Text])
  (:require [speclj.core :refer :all]
            [clojure.string :as s]
            [uap-clj.udf.hive.common-spec :refer :all]
            [uap-clj.udf.hive.device :refer [-evaluate]]))

(context "Unnown Device"
  (let [device-parser (uap-clj.udf.hive.Device.)
        ua (Text. unknown-useragent)
        [expected-family expected-brand expected-model]
          (s/split "Other\t\t" #"\t")
        [actual-family actual-brand actual-model]
          (s/split (str (#'-evaluate device-parser ua)) #"\t")]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as brand '%s'" expected-brand)
        (should= expected-brand actual-brand))
      (it (format "is categorized as model '%s'" expected-model)
        (should= expected-model actual-model)))))

(context "Known Device"
  (let [device-parser (uap-clj.udf.hive.Device.)
        ua (Text. known-useragent)
        [expected-family expected-brand expected-model]
          (s/split "Spider\tSpider\tDesktop" #"\t")
        [actual-family actual-brand actual-model]
          (s/split (str (#'-evaluate device-parser ua)) #"\t")]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as brand '%s'" expected-brand)
        (should= expected-brand actual-brand))
      (it (format "is categorized as model '%s'" expected-model)
        (should= expected-model actual-model)))))
