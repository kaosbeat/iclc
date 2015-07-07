(ns bohrbug.core
(:require [iclc.core :refer :all])
  (:use [overtone.live]
        [overtone.inst.drum]
        [overtone.inst.io]
        [overtone.inst.synth]
        [overtone.osc.util]
        [overtone.osc.peer]
        [overtone.osc.dyn-vars]
        ))

(def bus1 (audio-bus))
(def bus2 (audio-bus))
(def bus3 (audio-bus))
(def bus4 (audio-bus))
(def bus5 (audio-bus))

; kickA

(definst kickA [freq 105 dur 1.2 width 0.5 amp -20 out-bus 3]
  (let [freq-env (* freq (env-gen (perc 0.02 (* 0.49 dur))))
        env (env-gen (perc 0.019 dur) 1 1 0 1 FREE)
        sqr (* (env-gen (perc 0 0.0800)) (pulse (* 2 freq) width))
        src (sin-osc freq-env)
        src2 (sin-osc-fb freq-env)
        filt (lpf (+ sqr src src2) 100)
        drum (+ sqr (* env filt))]
        (compander drum drum 0.2 1 0.1 0.01 0.01)
        ))

 ; c-hat

(definst c-hat [amp 0.7 t 0.03]
  (let [env (env-gen (perc 0.001 t) 1 1 0 1 FREE)
             noise (white-noise)
             sqr (* (env-gen (perc 0.07 0.04)) (pulse 880 0.8))
             filt (rhpf  (+ sqr noise) 300 0.5)
       ]
             (* amp env filt)
             ))

(defsynth fmchord [carrier 440 divisor 4.0 depth 2.0 out-bus 0]
  (let [modulator (/ carrier divisor)
        mod-env (env-gen (lin 1.9 3.8 -2.8))
        amp-env (env-gen (lin 1 0 4.0 1) :action FREE)
        filt (rlpf (+ carrier modulator ) 100 0.1)]
    (out bus2  (pan2 (* 0.15 amp-env
                          (sin-osc (+ carrier
                                      (* mod-env (* carrier depth) (sin-osc modulator)))))))
    ))

; ___  __  __   _____
;| __||  \/  | |_   _|___  _ _   ___  ___
;| _| | |\/| |   | | / _ \| ' \ / -_ (_-<
;|_|  |_|  |_|   |_| \___/|_||_|\___|/__/


(defsynth fmtones [carrier 440 divisor 8.0 depth 8.0 out-bus 1]
  (let [modulator (/ carrier divisor)
        mod-env (env-gen (lin-rand -0.2 0.4 -2.8))
        amp-env (env-gen (lin 0 -0.2 0.1 1 ) :action FREE)
        filt (glitch-rhpf:ar (+ carrier modulator ) 500 2.6)
             ]
      (out bus1 (pan2 (* 0.60 amp-env
                          (lf-saw (+ carrier
                                     (* mod-env (* carrier depth) (sin-osc  modulator)))))))))

;kick control
(def q {:amp 0.5 :dur 1.0})
(def x {:amp 2.0 :dur 4.0})
(def y {:freq 75})
;; (def z {:amp})

;hh control
(def o {:amp 0.2})
(def p {:amp 0.5})
(def s {:t 0.18})

;fmtones control
 (def -a {:carrier 261.63 :car-freq 261.63})
 (def -b {:carrier 277.18})
 (def -c {:carrier 293.66})
 (def -d {:carrier 311.13})
 (def -e {:carrier 329.63})
 (def -f {:carrier 349.23})
 (def -g {:carrier 369.99})
 (def -h {:carrier 392.00})
 (def -i {:carrier 415.00})
 (def -j {:carrier 440.00})
 (def -k {:carrier 466.16})
 (def -l {:carrier 493.88})
 (def a {:carrier 523.25
         :divisor (ranged-rand 5.0 9.0) :depth (ranged-rand 0.1 9.0)
         })
 (def b {:carrier 554.37})
 (def c {:carrier 587.33 :car-freq 587.33})
 (def d {:carrier 622.25 :car-freq 622.25})
 (def e {:carrier 659.25})
 (def f {:carrier 698.46 :depth (ranged-rand 0.2 0.8)})
 (def g {:carrier 739.99})
 (def h {:carrier 783.99
         :depth 9.0
         :mix 0.5
         }
   )
 (def i {:carrier 830.61})
 (def j {:carrier 880.00})
 (def k {:carrier 923.33})
 (def l {:carrier 987.77})
 (def +a {:carrier 1046.50})
 (def +b {:carrier 1108.73})
 (def +c {:carrier 1174.66})
 (def +d {:carrier 1244.51})

;sequencer
(defn flatten1

  [m]
  (reduce (fn [r [arg val]] (cons arg (cons val r))) [] m))

(def live-pats (atom pats))


(defn live-sequencer
  ([curr-t sep-t live-patterns] (live-sequencer curr-t sep-t live-patterns 0))
  ([curr-t sep-t live-patterns beat]
     (doseq [[sound pattern] @live-patterns
             :let [v (nth pattern (mod beat (count pattern)))
                   v (cond
                      (= 1 v)
                      []

                      (map? v)
                      (flatten1 v)

                      :else
                      nil)]
             :when v]
       (at curr-t (apply sound v)))
     (let [new-t (+ curr-t sep-t)]
       (apply-by new-t #'live-sequencer [new-t sep-t live-patterns (inc beat)]))))

(live-sequencer (now) 100 live-pats)
(def metro (metronome 150))
(metro 150)
