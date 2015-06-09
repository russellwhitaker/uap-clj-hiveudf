;;;
;;; This code adapted from a sample courtesy of Dr. Christian Betz.
;;;
(ns kryo-ext.genericudf-serializer
  (:import [com.esotericsoftware.kryo Kryo]
           [com.esotericsoftware.kryo.io Output Input])
  (:gen-class
   :name kryo.ext.GenericUDFSerializer
   :extends com.esotericsoftware.kryo.Serializer))

(defn -write
  [_ ^Kryo _ ^Output _ _]
  )

(defn -read
  [_ ^Kryo _ ^Input _ class]
  (.newInstance class))
