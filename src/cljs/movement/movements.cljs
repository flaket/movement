(ns movement.movements)

;; -------------------------
;; Movements in categories
; Warmup
(def warmup [:joint-mobility :jump-rope :running])

; Mobility
(def hip-mobility [:squat-routine :squat-routine-2.0 :movnat-routine])
(def shoulder-mobility [:shoulder-rom-stabilisation :scapula-mobilisation])
(def wrist-mobility [:wrist-prep])
(def ankle-mobility [:ankle-prep])
(def spine-mobility [:bridge-rotation :locked-knees-rounded-back-deadlift])

; Hanging
(def hanging [:passive-hang :active-hang :false-grip-hang :side-to-side-swing
              :arching-active-hang :front-stationary-swing
              :one-arm-passive :one-arm-active :shawerma
              :swing-grip-routine :figure-8])
; Locomotion
(def locomotion [:swing-to-handstand :cart-wheel :handstand-walk :bridge-walk
                 :duck-walk :horse-walk :lizard-crawl :ostrich-walk :bear-walk
                 :crab-walk])
; Equilibre
(def equilibre [:gatherings :wall-walk :wall-kick :handstand-walk
                :handstand-push-up :air-baby :qdr])
; Leg strength
(def leg-strength [:basic-squat :back-squat :front-squat :overhead-squat
                   :basic-lunge :back-lunge :front-lunge :overhead-lunge
                   :deadlift :pistols :shrimp :behind-leg-squat
                   :jump-onto-box-standing :jump-onto-box-squatting
                   :explosive-flipping :natural-leg-curl])
; Auxiliary strength
(def auxiliary [:l-sit :straddle :v-up :sitting-leg-lift :swedish-leg-lift
                :hanging-leg-lift :gatherings :archups])
; Straight arm scapular strength
(def sass [:swedish-bar-hold-front :swedish-bar-hold-back
           :back-lever :front-lever :side-lever :planche :handstand])
; Bent arm strength
(def bas [:push-up-basic :push-up-russian :push-up-wide
          :push-up-diamond :push-up-hindu :push-up-lateral :push-up-bridge
          :push-up-archer :push-up-one-arm :push-up-one-leg-one-arm
          :dips-basic :dips-russian :dips-single-bar :dips-korean :dips-ring
          :dips-ring-wide :dips-ring-archer
          :handstand-push-up-head-wall :handstand-push-up-wall :handstand-push-up-free
          :push-up-planche :pull-up-basic :pull-up-wide :pull-up-rings :pull-up-rings-wide
          :pull-up-chest :pull-up-waist :pull-up-weighted :pull-up-scapula
          :one-arm-pull-up-forearm :one-arm-pull-up-bicep :one-arm-negative
          :archer-pull-up :one-arm-pull-up-shoulder
          :one-arm-pull-up :row-basic :row-wide :front-lever-row
          :german-hang-pull :pull-over :front-lever-pull :back-lever-pull
          :tick-tock :back-lever-negative :front-lever-negative
          :muscle-up :false-grip-pull-up :muscle-up-negative
          :muscle-up-l-sit :rope-climb])

(def movnat-sitting [:split-stand-to-split-sit :walking-split-squat :side-shuffle
                     :stepping-under :stepping-over :hinged-walk :split-squat
                     :squat-to-medium-kneeling :squat-to-kneeling :kneeling-to-tall-kneeling
                     :kneeling-to-tall-half-kneeling :kneeling-to-lateral-half-kneeling
                     :split-knee-walk :tall-half-kneeling-to-tall-split-kneeling
                     :deep-squat :deep-knee-bend :deep-squat-to-deep-knee-bend
                     :deep-squat-narrow-base :tall-half-kneeling-to-kneeling
                     :tall-split-kneeling-to-bent-sit :deep-knee-bend-to-kneeling
                     :stepping-up :backward-stepping-down :extended-stepping-up
                     :forward-stepping-down])
(def movnat-balancing [:tip-toe-balancing :balancing-walk :balancing-split-squat
                       :balancing-shuffle :cross-reverse :pivot-reverse
                       :split-squat-pivot-reverse :balancing-tripod-transition
                       :balancing-foot-hand-crawl])
(def movnat-crawling [:inverted-crawl :knee-elbow-crawl :knee-hand-crawl :foot-elbow-crawl
                      :foot-hand-crawl :push-pull-crawl :rocking :rotational-rocking
                      :bent-sit-to-lateral-half-kneeling :shoulder-crawl :hip-thrust-crawl
                      :sit-to-backward-roll :lying-to-forward-roll :forward-roll :tripod-transition
                      :backward-sprawl :forward-sprawl-to-push-up])
(def movnat-jumping [:leg-swing-jump :broad-jump :split-jump :vertical-jump :depth-jump :lateral-side-jump])
(def movnat-climbing [:tuck-swing :tap-swing :side-swing :side-swing-traverse :side-swing-hang-hold
                      :side-swing-power-traverse :pull-up :pull-up-reach :pull-up-hold :one-arm-dead-hang
                      :jumping-pull-up :hanging-side-foot-lift :hanging-leg-hook-over
                      :hanging-knee-tuck :hanging-knee-to-bar :hanging-front-foot-lift
                      :front-swing-traverse :front-power-traverse :elbow-pull-up :dead-hang
                      :sliding-swing-up :elbow-pop-up :hand-swing :swing-pop-up :tuck-pop-up
                      :muscle-up :roll-up])
(def movnat-lifting [:dead-lift :lapping :waist-carry :chest-carry :hand-carry :shoulder-carry
                     :log-shouldering :shoulder-carry-squat :hands-free-shoulder-carry-squat
                     :shoulder-carry-switch :clean :jerk :push-press])
(def movnat-throwing [:chest-throw :rotational-throw :front-swing-throw :overhead-throw])

(def floreio [])

(def running [:sprint :interval :5K])
(def hiking [:hiking])
(def parkour [])
(def swimming [:swimming])
(def rock-climbing [:bouldering :rock-climbing])
(def ball-sport [:squash :football])

(def mobility (concat hip-mobility shoulder-mobility
                      wrist-mobility ankle-mobility spine-mobility))
(def strength (concat leg-strength auxiliary sass bas))
(def movnat (concat movnat-sitting movnat-balancing movnat-crawling movnat-jumping movnat-climbing
                    movnat-lifting movnat-throwing))
(def movnat-warmup (concat movnat-sitting movnat-balancing movnat-crawling))