(ns uap-clj.udf.hive.browser-spec
  (:import [org.apache.hadoop.io Text])
  (:require [speclj.core :refer :all]
            [clojure.string :as s]
            [uap-clj.udf.hive.common-spec :refer :all]
            [uap-clj.udf.hive.browser :refer [-evaluate]]))

(context "Unknown Browser"
  (let [browser-parser (uap-clj.udf.hive.Browser.)
        ua (Text. unknown-useragent)
        [expected-family expected-major expected-minor expected-patch]
          (s/split "Other\t\t\t" #"\t")
        [actual-family actual-major actual-minor actual-patch]
          (s/split (str (#'-evaluate browser-parser ua)) #"\t")]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as major '%s'" expected-major)
        (should= expected-major actual-major))
      (it (format "is categorized as minor '%s'" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "is categorized as patch '%s'" expected-patch)
        (should= expected-patch actual-patch)))))

(context "Known Browser"
  (let [browser-parser (uap-clj.udf.hive.Browser.)
        ua (Text. known-useragent)
        [expected-family expected-major expected-minor expected-patch]
          (s/split "Crawler\t\t\t" #"\t")
        [actual-family actual-major actual-minor actual-patch]
          (s/split (str (#'-evaluate browser-parser ua)) #"\t")]
    (describe (format "A user agent '%s'" known-useragent)
      (it (format "is categorized as family '%s'" expected-family)
        (should= expected-family actual-family))
      (it (format "is categorized as major '%s'" expected-major)
        (should= expected-major actual-major))
      (it (format "is categorized as minor '%s'" expected-minor)
        (should= expected-minor actual-minor))
      (it (format "is categorized as patch '%s'" expected-patch)
        (should= expected-patch actual-patch)))))
