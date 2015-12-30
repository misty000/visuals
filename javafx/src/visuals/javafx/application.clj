(ns visuals.javafx.application
  "JavaFX Startup utilities"
  (:gen-class
    :extends javafx.application.Application))

(defonce root-stage (promise))

(defn -start
  [this stage]
  (deliver root-stage stage))





