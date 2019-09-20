(ns wireguard-switcher.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]])
  (:import [dorkbox.systemTray SystemTray MenuItem Separator Checkbox]
           [java.awt.event ActionListener])
  (:gen-class))

(def version "0.1")

(def cli-options
  [
   ["-h" "--help" "Print the command line help"]
   ["-v" "--version" "Print the version string and exit"]])

(def resources
  {:dark (io/resource "lock.png")
   :red (io/resource "lock-red.png")
   :green (io/resource "lock-green.png")
   :light (io/resource "lock-light-grey.png")})

(defmacro add-menu-item [menu label & body]
  `(let [item# (MenuItem. ~label
                    (proxy [ActionListener] []
                      (actionPerformed [~'_]
                        ~@body)))]
     (.add ~menu item#)
     item#))

(defmacro add-menu-checkbox [menu label & body]
  `(let [item# (Checkbox. ~label
                    (proxy [ActionListener] []
                      (actionPerformed [~'_]
                        ~@body)))]
     (.add ~menu item#)
     item#))

(defmacro add-menu-separator [menu]
  `(.add ~menu (Separator.)))

(defn setup-switcher [items]
  (when-let [tray (SystemTray/get)]
    (let [icon (:dark resources)]
      (doto tray
        (.setImage (:dark resources))
        (.setStatus "Wireguard: Disconnected"))

      (let [menu (.getMenu tray)]
        (add-menu-separator menu)
        (doseq [item items]
          (add-menu-item menu item))
        (add-menu-separator menu)
        (add-menu-item menu "Quit"
                       (.shutdown tray))))))

(defn read-wireguard-config-dir [& [dir]]
  (-> dir
      (or "/etc/wireguard")
      io/file
      file-seq
      (->> (filter #(not (.isDirectory %)))
           (map #(-> % .getName
                     (->> (re-find #"(.+)\.conf")
                          second)))
           (into []))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      (println summary)

      (:version options)
      (println "Version:" version)

      :else
      (-> (read-wireguard-config-dir)
          setup-switcher))))
