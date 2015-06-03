(ns uap-clj.udf.hive.device
  (:import [org.apache.hadoop.hive.ql.exec UDF]
           [org.apache.hadoop.io Text])
  (:require [uap-clj.core :refer [extract-device-fields regexes-device]])
  (:gen-class
   :name uap-clj.udf.hive.Device
   :extends org.apache.hadoop.hive.ql.exec.UDF
   :methods [[evaluate [org.apache.hadoop.io.Text] org.apache.hadoop.io.Text]]))

(defn #^Text -evaluate
  "Extract Device family<tab>brand<tab>model from a useragent string

   USAGE: after adding the .jar file from this project, create your temporary function
     (e.g. as temporary function device()), then use Hive split(device(agentstring), '\\t')
     to break into 3 fields in your query, returning a Hive array<string> of length 3 which
     can be stored in an array<string> field and later referenced in a hive query by index, e.g.:

     SELECT os[0] AS device_family,
            os[1] AS device_brand,
            os[2] AS device_model
     FROM parsed_useragent;
  "
  [this #^Text s]
  (when s
    (Text.
      (try
        (let [ua (extract-device-fields (.toString s) regexes-device)]
          (str (or (get ua :family nil) "") \tab
               (or (get ua :brand nil) "") \tab
               (or (get ua :model nil) "")))
      (catch Exception e (str (.getMessage e) ": " (.toString s)))))))
