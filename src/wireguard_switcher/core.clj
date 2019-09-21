(ns wireguard-switcher.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.java.shell :as shell]
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

(defn read-wireguard-state []
  (let [{:keys [out err exit]} (shell/sh "sudo" "wg")]
    (when (zero? exit)
      (-> out
          (string/split #"\n")
          (->> (filter #(re-find #"^interface: " %))
               (map #(->> %
                          (re-find #"^interface: (.+)")
                          second))
               (into #{}))))))

(defn change-wireguard-state [conn state]
  (-> (shell/sh "sudo" "service" (str "wg-quick@" conn) (if state "start" "stop"))
      :exit
      zero?))

(defn toggle-wireguard-state [conn]
  (let [states (read-wireguard-state)
        connected? (states conn)]
    (if connected?
      (change-wireguard-state conn false)
      (change-wireguard-state conn true))))

(defn set-check-marks-from-state [{:keys [tray menu-items]}]
  (let [state (read-wireguard-state)
        any-connections? (seq state)]
    (doto tray
      (.setImage (if any-connections? (:green resources) (:red resources)))
      (.setStatus (str "Wireguard: "
                       (if any-connections? "Connected" "Disconnected"))))

    (doseq [[conn checkbox] menu-items]
      (.setChecked checkbox (boolean (state conn))))))

(defn setup-switcher [items connected]
  (when-let [tray (SystemTray/get)]
    (let [icon (:dark resources)]
      (doto tray
        (.setImage (:dark resources))
        (.setStatus (str "Wireguard: " (if (seq connected) "Connected" "Disconnected"))))

      (let [menu (.getMenu tray)]
        (add-menu-separator menu)
        (let [menu-items (into {}
                               (for [item items]
                                 (let [i (add-menu-checkbox
                                          menu item
                                          (toggle-wireguard-state item))]
                                   (.setChecked i (boolean (connected item)))
                                   [item i])))]
          (add-menu-separator menu)
          (add-menu-item menu "Quit"
                         (.shutdown tray)
                         (System/exit 0))
          {:tray tray
           :menu-items menu-items})))))

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
  "main entry point for system tray menu"
  [& args]
  (let [{:keys [options summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      (println summary)

      (:version options)
      (println "Version:" version)

      :else
      (let [menu (setup-switcher
                  (read-wireguard-config-dir)
                  (read-wireguard-state))]
        (loop []
          (set-check-marks-from-state menu)
          (Thread/sleep 1000)
          (recur))))))
