(ns gainful3-parse.utils.logging
  (:require [clojure.tools.logging :as log]))

(defn log
  [printable]
  (log/info printable))

(defmacro try-log
  [tag call]
  `(try ~call
        (catch Exception e# (log/warn (str "Exception when calling "
                                           '~call
                                           " with objective '"
                                           ~tag
                                           "', error msg was "
                                           (.getMessage e#))))))

