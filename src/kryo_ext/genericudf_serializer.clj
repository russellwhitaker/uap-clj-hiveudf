;;;
;;; Adapted from a sample courtesy of Dr. Christian Betz
;;; http://twitter.com/Chris_Betz
;;;
;;; Why this is necessary:
;;;   https://issues.apache.org/jira/browse/HIVE-7711
;;;   "Error Serializing GenericUDF"
;;;
(ns kryo-ext.genericudf-serializer
  (:import [org.apache.hive.com.esotericsoftware.kryo Kryo]
           [org.apache.hive.com.esotericsoftware.kryo.io Output Input])
  (:gen-class
   :name kryo.ext.GenericUDFSerializer
   :extends org.apache.hive.com.esotericsoftware.kryo.Serializer))

(defn -write
  [_ ^Kryo _ ^Output _ _]
  )

(defn -read
  [_ ^Kryo _ ^Input _ class]
  (.newInstance class))
