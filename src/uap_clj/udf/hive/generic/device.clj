(ns uap-clj.udf.hive.generic.device
  (:import [org.apache.hadoop.hive.ql.udf UDFType]
           [org.apache.hadoop.hive.ql.udf.generic GenericUDF
                                                  GenericUDF$DeferredObject]
           [java.util ArrayList]
           [org.apache.hadoop.io Text]
           [org.apache.hadoop.hive.serde2.objectinspector ObjectInspectorFactory]
           [org.apache.hadoop.hive.ql.exec Description])
  (:require [uap-clj.core :refer [extract-device-fields regexes-device]]
            [uap-clj.udf.hive.generic.common :refer [device-fieldnames
                                                     check-arguments
                                                     get-struct-field-ois]])
  (:gen-class
   :name ^{org.apache.hadoop.hive.ql.udf.UDFType {:deterministic true}
           org.apache.hadoop.hive.ql.exec.Description
             {:name "device"
              :value "Takes a useragent & returns struct<family,brand,model>"
              :extended "(type: struct<family:string,brand:string,model:string>)"}}
         uap-clj.udf.hive.generic.Device
   :extends org.apache.hadoop.hive.ql.udf.generic.GenericUDF
   :init init
   :state state))

(defn -init []
  [[] (atom {})])

(defn -initialize
  [this object-inspectors]
  (check-arguments this object-inspectors)
  (swap! (.state this)
         assoc :element-oi (aget object-inspectors 0))
  (let [struct-fieldnames device-fieldnames
        struct-field-object-inspectors (get-struct-field-ois struct-fieldnames)]
    (ObjectInspectorFactory/getStandardStructObjectInspector struct-fieldnames
                                                             struct-field-object-inspectors)))

(defn -evaluate
  [this #^"[Lorg.apache.hadoop.hive.ql.udf.generic.GenericUDF$DeferredObject;" arguments]
  (when arguments
    (let [ua (extract-device-fields (.toString (.get (aget arguments 0))) regexes-device)]
      (object-array (vec (map #(Text. (str (get ua (keyword %) nil))) device-fieldnames))))))

(defn -getDisplayString
  [_ children]
  (format "Looks up useragent device fields for '%s'" (aget children 0)))
