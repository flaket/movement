(ns movement.data)

(def all-categories [:leg-strength
                     :hip-mobility
                     :l-sit
                     :push-up
                     :lift
                     :push
                     :lunge
                     :run
                     :shoulder-mobility
                     :walk
                     :ring
                     :front-lever
                     :straight-arm-strength
                     :dip
                     :rope
                     :crawl
                     :side-lever
                     :bar
                     :bent-arm-strength
                     :planche
                     :natural
                     :climb
                     :squat
                     :elastic-band
                     :wrist-mobility
                     :sit-stand-transition
                     :muscle-up
                     :single-leg
                     :balance
                     :bridge
                     :spine-mobility
                     :mobility
                     :brachiate
                     :hang
                     :weight
                     :footwork
                     :handstand
                     :balancing-locomotion
                     :throw
                     :acrobatic
                     :core
                     :roll
                     :pull-up
                     :jump-rope
                     :carry
                     :beam
                     :back-lever
                     :pull
                     :ankle-mobility
                     :jump
                     :leg-lift
                     :headstand
                     :air-baby])

(def all-movements [{:name "Beinsving sideveis",
                     :image "side-leg-swing.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:name "Dra fra flådd katte",
                     :image "pull-from-german-hang.png",
                     :measurement "repetitions",
                     :category #{:ring :straight-arm-strength :bar}}
                    {:previous ["Spark til håndstående"],
                     :image "l-handstand.png",
                     :measurement "duration",
                     :category #{:shoulder-mobility :wrist-mobility :handstand},
                     :name "L-håndstående"}
                    {:next ["Push-up"],
                     :image "knee-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Push-up på knærne"}
                    {:next ["Push-up en arm"],
                     :previous ["Vid push-up" "Push-up diamant"],
                     :image "archer-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Bueskytter push-up"}
                    {:name "Henge til flådd katte",
                     :image "hanging-to-german-hang.png",
                     :measurement "repetitions",
                     :category #{:ring :straight-arm-strength :bar}}
                    {:name "Dynamisk ankelfleksjon",
                     :image "dynamic-reach.png",
                     :measurement "repetitions",
                     :category #{:mobility :ankle-mobility}}
                    {:next ["Vid push-up"
                            "Push-up diamant"
                            "Pseudo-planche push-up"
                            "Hindu push-up"
                            "Russisk push-up"
                            "Push-up på fingertuppene"
                            "Sideveis push-up"
                            "Push-up i ringer"],
                     :previous ["Push-up på knærne"],
                     :image "push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Push-up"}
                    {:previous ["V-up"], :image "v-up.png", :measurement "repetitions", :category #{:core}, :name "V-up"}
                    {:name "Liggende til foroverrulle",
                     :image "lying-to-forward-roll.png",
                     :measurement "repetitions",
                     :category #{:natural :roll}}
                    {:next ["Kravle på albuer og knær"],
                     :image "baby-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Kravle på hender og knær"}
                    {:next ["Rotasjon inn i lav bro"],
                     :image "slide-into-low-bridge.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :bridge :spine-mobility :mobility},
                     :name "Skli inn i lav bro"}
                    {:previous ["Sit-up"],
                     :image "sit-up-pike.png",
                     :measurement "repetitions",
                     :category #{:core},
                     :name "Sit-up til tærne"}
                    {:name "Over hodet utfall",
                     :image "overhead-lunge.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :lunge :weight}}
                    {:next ["Hengende benløft"],
                     :previous ["Liggende benløft"],
                     :image "incline-bench-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:l-sit :core},
                     :name "Skråbenk benløft"}
                    {:next ["Balansere lavt sideveis"],
                     :previous ["Balansegang" "Balansere baklengs" "Balansere sideveis"],
                     :image "balancing-low-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Lav balansegang"}
                    {:next ["Støt"],
                     :previous ["Skulderpress"],
                     :image "push-press.png",
                     :measurement "repetitions",
                     :category #{:lift :push :natural :weight},
                     :name "Push-press"}
                    {:name "Utvendig rotasjon med strikk",
                     :image "band-external-rotation.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:name "Knebøy bønn",
                     :image "prayer-squat.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :spine-mobility :mobility}}
                    {:next ["Håndstående push-up i ringer"],
                     :previous ["Vegg håndstående push-up med spark"],
                     :image "wall-handstand-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Vegg håndstående push-up"}
                    {:name "Splitt sitt ned",
                     :image "split-sit.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :natural :sit-stand-transition :mobility}}
                    {:name "Flies med strikk",
                     :image "band-side-fly.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:previous ["Håndstående push-up" "Hodestående rett benløft"],
                     :image "back-extend-to-handstand.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :handstand :headstand},
                     :name "Benløft til håndstående"}
                    {:previous ["Balanserende sniking"],
                     :image "lateral-stealth-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Balanserende sideveis sniking"}
                    {:name "Rett deg ut",
                     :image "straighten-into-line.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :handstand}}
                    {:next ["Håndstående len"],
                     :image "planche-lean.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :planche},
                     :name "Planche len"}
                    {:next ["Pistol"],
                     :previous ["Hevet knebøy splitt"],
                     :image "chair-pistol-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg},
                     :name "Pistol med stol"}
                    {:name "Jefferson curl",
                     :image "jefferson-curl.png",
                     :measurement "repetitions",
                     :category #{:spine-mobility :mobility :weight}}
                    {:next ["Krabbekravle baklengs"],
                     :image "inverted-crawl.png",
                     :measurement "distance",
                     :category #{:crawl},
                     :name "Krabbekravle"}
                    {:previous ["Hengende sideløft" "Svinge sideveis"],
                     :image "hanging-leg-hook.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb :leg-lift},
                     :name "Hengende fotkrok"}
                    {:next ["Front-lever"],
                     :previous ["Strekk ben front-lever"],
                     :image "straddle-front-lever.png",
                     :measurement "duration",
                     :category #{:ring :front-lever :straight-arm-strength :bar},
                     :name "Straddle front-lever"}
                    {:next ["L-sit"],
                     :previous ["Tuck l-sit på stang"],
                     :image "parallell-bar-l-sit.png",
                     :measurement "duration",
                     :category #{:l-sit :core},
                     :name "L-sit på stang"}
                    {:next ["Flat front-lever"],
                     :image "tuck-front-lever.png",
                     :measurement "duration",
                     :category #{:ring :front-lever :straight-arm-strength :bar},
                     :name "Tuck front-lever"}
                    {:next ["Enkle hopp"],
                     :image "double-under.png",
                     :measurement "repetitions",
                     :category #{:footwork :jump-rope},
                     :name "Doble hopp"}
                    {:previous ["Bjørnekravle"],
                     :image "balancing-bear-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural :balance :balancing-locomotion :beam},
                     :name "Balanserende bjørnekravle"}
                    {:next ["Hengende 90 benløft"],
                     :previous ["Liggende benløft" "Tærne til stanga"],
                     :image "hanging-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:l-sit :core},
                     :name "Hengende benløft"}
                    {:previous ["Bjørnekravle baklengs"],
                     :image "lateral-bear-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Bjørnekravle sideveis"}
                    {:previous ["Hevet kryssende pistol"],
                     :image "crossing-pistol-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg :balance},
                     :name "Kryssende pistol"}
                    {:name "Frontbøy",
                     :image "front-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :natural :squat :weight}}
                    {:next ["Tærne til stanga"],
                     :image "hanging-knee-tuck.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb :leg-lift},
                     :name "Hengende kneløft"}
                    {:name "Bære i hendene", :image "hand-carry.png", :measurement "distance", :category #{:natural :weight :carry}}
                    {:next ["Reke"],
                     :previous ["Reke med leggen"],
                     :image "knee-shrimp-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg :balance},
                     :name "Reke med kneet"}
                    {:next ["Hodestående rett benløft"],
                     :previous ["Hodestående"],
                     :image "headstand-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:core},
                     :name "Hodestående benløft"}
                    {:next ["Eksplosiv pull-up" "Bueskytter pull-up"],
                     :previous ["Pull-up"],
                     :image "chest-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :pull-up :pull},
                     :name "Pull-up til bryst"}
                    {:next ["Hengende fotkrok"],
                     :previous ["Tærne til stanga"],
                     :image "hanging-side-foot-lift.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb :leg-lift},
                     :name "Hengende sideløft"}
                    {:previous ["Rotasjon inn i lav bro" "Høy bro"],
                     :image "rotate-into-high-bridge.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :bridge :spine-mobility :mobility},
                     :name "Rotasjon inn i høy bro"}
                    {:name "Knebøy hold mot taket",
                     :image "sky-reach-hold.png",
                     :measurement "duration",
                     :category #{:hip-mobility :spine-mobility :mobility}}
                    {:next ["Push press"],
                     :image "shoulder-press.png",
                     :measurement "repetitions",
                     :category #{:lift :push :natural :weight},
                     :name "Skulderpress"}
                    {:previous ["Dip"],
                     :image "single-bar-dip.png",
                     :measurement "repetitions",
                     :category #{:push :dip :bar :bent-arm-strength :natural :climb},
                     :name "Dip på enkel stang"}
                    {:next ["Bjørnekravle sideveis"],
                     :previous ["Bjørnekravle"],
                     :image "backward-bear-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Bjørnekravle baklengs"}
                    {:previous ["Reke med kneet"],
                     :image "shrimp-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg :balance},
                     :name "Reke"}
                    {:next ["Muscle-up"],
                     :image "negative-muscle-up.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb :muscle-up},
                     :name "Negativ muscle-up"}
                    {:previous ["Krabbekravle"],
                     :image "balancing-inverted-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural :balance :balancing-locomotion :beam},
                     :name "Balanserende krabbekravle"}
                    {:next ["Pistol med stol"],
                     :image "elevated-split-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg},
                     :name "Hevet knebøy splitt"}
                    {:next ["Rotasjon inn i lav bro"],
                     :image "wall-rotation.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :spine-mobility :mobility},
                     :name "Rotasjon mot veggen"}
                    {:previous ["Svinge sideveis hold"],
                     :image "swinging-arm-lateral-traverse.png",
                     :measurement "distance",
                     :category #{:straight-arm-strength :bar :natural :brachiate :hang},
                     :name "Traversere svingende sideveis"}
                    {:previous ["Planke" "Sideplanke"],
                     :image "one-arm-plank.png",
                     :measurement "duration",
                     :category #{:core},
                     :name "Planke på en arm"}
                    {:name "Trekk ned med strikk",
                     :image "band-overhead-pull-down.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:previous ["Henge passivt til aktivt"],
                     :image "elbow-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb},
                     :name "Pull-up fra albuene"}
                    {:name "Knele til høy knele",
                     :image "kneeling-to-tall-kneeling.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :sit-stand-transition :mobility}}
                    {:name "Kubansk rotasjon",
                     :image "cuban-rotation.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :mobility :weight}}
                    {:previous ["Pull-up" "Svinge sideveis hold"],
                     :image "bent-arm-lateral-traverse.png",
                     :measurement "distance",
                     :category #{:bar :bent-arm-strength :natural :brachiate :hang},
                     :name "Traversere sideveis"}
                    {:next ["Hodestående"],
                     :previous ["Frosk"],
                     :image "frog-stance-to-headstand.png",
                     :measurement "repetitions",
                     :category #{:wrist-mobility :headstand},
                     :name "Frosk til hodestående"}
                    {:next ["Push-up i vinkel"],
                     :previous ["Push-up"],
                     :image "hindu-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Hindu push-up"}
                    {:previous ["Straddle side-lever"],
                     :image "side-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :side-lever :bar},
                     :name "Side-lever"}
                    {:next ["Strekk ben planche"],
                     :previous ["Tuck planche"],
                     :image "flat-planche.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :planche},
                     :name "Flat planche"}
                    {:name "Baklengs turnrulle", :image "gymnastic-backward-roll.png", :measurement "repetitions", :category #{:acrobatic}}
                    {:previous ["Push-up"],
                     :image "fingertip-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Push-up på fingertuppene"}
                    {:next ["Kryssende pistol"],
                     :previous ["Pistol med stol"],
                     :image "elevated-crossing-pistol-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg :balance},
                     :name "Hevet kryssende pistol"}
                    {:next ["Air baby fingertupper"],
                     :previous ["Air baby rotasjon"],
                     :image "static-air-baby-two-arms.png",
                     :measurement "duration",
                     :category #{:wrist-mobility :air-baby},
                     :name "Air baby begge hender"}
                    {:previous ["Svinge sideveis hold"],
                     :image "swinging-linear-traverse.png",
                     :measurement "distance",
                     :category #{:straight-arm-strength :bar :natural :brachiate :hang},
                     :name "Traversere svingende"}
                    {:name "Rygghev", :image "arch-up.png", :measurement "repetitions", :category #{:core}}
                    {:name "Gående utfall", :image "walking-lunge.png", :measurement "distance", :category #{:walk :natural}}
                    {:next ["Vid dip i ringer"],
                     :previous ["Dip" "Støtteposisjon i ringer"],
                     :image "ring-dip.png",
                     :measurement "repetitions",
                     :category #{:push :ring :dip :bent-arm-strength},
                     :name "Dip i ringer"}
                    {:next ["En arm pull-up"],
                     :previous ["Assistert en arm pull-up"],
                     :image "negative-one-arm-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :pull-up :pull},
                     :name "Negativ en arm pull-up"}
                    {:previous ["Svinge sideveis"],
                     :image "release-hand-lateral-swing.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :brachiate :hang},
                     :name "Svinge sideveis slipp"}
                    {:next ["Henge aktivt med en arm"],
                     :previous ["Henge passivt"],
                     :image "one-arm-passive-hang.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :hang},
                     :name "Henge passivt med en arm"}
                    {:previous ["Push-up"],
                     :image "burpee.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Burpee"}
                    {:next ["Vid chin-up i ringer" "Falskt grep chin-up" "Assistert pull-up i en arm"],
                     :image "ring-chin-up.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :pull-up :pull},
                     :name "Chin-up i ringer"}
                    {:next ["Dip"],
                     :image "negative-dip.png",
                     :measurement "repetitions",
                     :category #{:push :dip :bar :bent-arm-strength :natural :climb},
                     :name "Negativ dip"}
                    {:previous ["Push-up"],
                     :image "lateral-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Sideveis push-up"}
                    {:next ["Straddle planche"],
                     :previous ["Flat planche"],
                     :image "one-leg-extended-planche.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :planche},
                     :name "Strekk ben planche"}
                    {:previous ["Sideveis hopp med tilløp" "Hopp opp"],
                     :image "front-flip.png",
                     :measurement "repetitions",
                     :category #{:acrobatic :jump},
                     :name "Salto"}
                    {:previous ["Balanserende vending" "Balanserende splittknebøy"],
                     :image "balancing-split-squat-turn.png",
                     :measurement "repetitions",
                     :category #{:natural :balance :beam},
                     :name "Balanserende vending i splittknebøy"}
                    {:previous ["Push-up"],
                     :image "diamond-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Push-up diamant"}
                    {:next ["Skråbenk benløft" "Sittende benløft"],
                     :image "lying-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:l-sit :core},
                     :name "Liggende benløft"}
                    {:name "Knebøy",
                     :image "squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :hip-mobility :natural :sit-stand-transition :mobility}}
                    {:next ["Henge passivt til aktivt"],
                     :previous ["Henge passivt"],
                     :image "active-hang.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :hang},
                     :name "Henge aktivt"}
                    {:previous ["Hopp med tilløp"],
                     :image "running-lateral-jump.png",
                     :measurement "repetitions",
                     :category #{:natural :jump},
                     :name "Sideveis hopp med tilløp"}
                    {:next ["Vegg håndstående push-up med spark"],
                     :previous ["Push-up i vinkel"],
                     :image "elevated-pike-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Hevet push-up i vinkel"}
                    {:previous ["Henge i falskt grep" "Ro"],
                     :image "false-grip-row.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :pull},
                     :name "Ro med falskt grep"}
                    {:name "Kaste med sving", :image "front-swing-throw.png", :measurement "repetitions", :category #{:natural :throw}}
                    {:previous ["Sideveis hopp med tilløp" "Hopp opp"],
                     :image "back-flip.png",
                     :measurement "repetitions",
                     :category #{:acrobatic :jump},
                     :name "Baklengs salto"}
                    {:next ["Straddle back-lever"],
                     :previous ["Flat back-lever"],
                     :image "one-leg-extended-back-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :bar :back-lever},
                     :name "Strekk ben back-lever"}
                    {:next ["Air baby"],
                     :previous ["Air baby begge hender"],
                     :image "static-air-baby-fingertips.png",
                     :measurement "duration",
                     :category #{:wrist-mobility :air-baby},
                     :name "Air baby fingertupper"}
                    {:next ["Flat planche"],
                     :previous ["Håndstående len"],
                     :image "tuck-planche.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :planche},
                     :name "Tuck planche"}
                    {:next ["Air baby rotasjon"],
                     :previous ["Frosk"],
                     :image "air-baby-extend-leg.png",
                     :measurement "repetitions",
                     :category #{:wrist-mobility :air-baby},
                     :name "Air baby strekk"}
                    {:previous ["Gå på veggen" "Rett deg ut"],
                     :image "kick-to-handstand.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :handstand},
                     :name "Spark til håndstående"}
                    {:next ["Back-lever"],
                     :previous ["Strekk ben back-lever"],
                     :image "straddle-back-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :bar :back-lever},
                     :name "Straddle back-lever"}
                    {:next ["Hengende 90 90 holde"],
                     :previous ["Hengende benløft"],
                     :image "hanging-90-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:l-sit :core},
                     :name "Hengende 90 benløft"}
                    {:name "Gå opp på",
                     :image "extended-stepping-up.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :hip-mobility :walk :natural :mobility}}
                    {:next ["Pull-up"],
                     :image "negative-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :natural :climb :pull-up :pull},
                     :name "Negativ pull-up"}
                    {:previous ["Pull-up" "Tærne til stanga"],
                     :image "pull-over.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb},
                     :name "Pull-over"}
                    {:next ["Flat side-lever"],
                     :previous ["Sideplanke" "Henge passivt til aktivt"],
                     :image "tuck-side-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :side-lever :bar},
                     :name "Tuck side-lever"}
                    {:previous ["Lav balansegang"],
                     :image "balancing-lateral-low-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Balansere lavt sideveis"}
                    {:name "Bære på skulderen", :image "shoulder-carry.png", :measurement "distance", :category #{:natural :weight :carry}}
                    {:next ["Balanserende vending i splittknebøy"],
                     :image "balancing-split-squat.png",
                     :measurement "repetitions",
                     :category #{:natural :balance :beam},
                     :name "Balanserende splittknebøy"}
                    {:previous ["Hengende 90 benløft"],
                     :image "hanging-90-90-hold.png",
                     :measurement "duration",
                     :category #{:l-sit :core},
                     :name "Hengende 90 90 holde"}
                    {:name "Markløft",
                     :image "deadlift.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :natural :weight}}
                    {:name "Utfall",
                     :image "lunge.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :hip-mobility :natural :mobility}}
                    {:name "Hestegange", :image "horse-stance-walk.png", :measurement "distance", :category #{:hip-mobility :mobility}}
                    {:name "Vektet knebøy",
                     :image "back-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :squat :weight}}
                    {:next ["Øglekravle"],
                     :previous ["Bjørnekravle"],
                     :image "spiderman-crawl.png",
                     :measurement "distance",
                     :category #{:push-up :push :crawl :bent-arm-strength},
                     :name "Spiderman-kravle"}
                    {:name "Støtteposisjon i ringer",
                     :image "ring-support-hold.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength}}
                    {:name "Gynge og rotere", :image "rotational-rocking.png", :measurement "repetitions", :category #{:core}}
                    {:next ["Chin-up" "Pull-up"],
                     :image "row.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :pull-up :pull},
                     :name "Ro"}
                    {:next ["Air baby"],
                     :previous ["Air baby begge hender"],
                     :image "static-air-baby.png",
                     :measurement "duration",
                     :category #{:wrist-mobility :air-baby},
                     :name "Air baby"}
                    {:next ["Hodestående benløft"],
                     :previous ["Frosk til hodestående"],
                     :image "headstand.png",
                     :measurement "repetitions",
                     :category #{:headstand},
                     :name "Hodestående"}
                    {:name "Planke", :image "plank.png", :measurement "duration", :category #{:core}}
                    {:next ["Air baby begge hender"],
                     :previous ["Air baby strekk"],
                     :image "air-baby-extend-leg.png",
                     :measurement "repetitions",
                     :category #{:wrist-mobility :air-baby},
                     :name "Air baby rotasjon"}
                    {:name "Lave flies med strikk",
                     :image "band-low-fly.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:next ["Svinge seg opp med hendene"],
                     :previous ["Svinge seg opp"],
                     :image "elbow-swing-up.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb},
                     :name "Svinge seg opp med albuene"}
                    {:next ["Svevende kravle"],
                     :previous ["Bjørnekravle"],
                     :image "gallop-crawl.png",
                     :measurement "distance",
                     :category #{:crawl},
                     :name "Galopperende kravle"}
                    {:name "Supermann gynge", :image "superman-rocking.png", :measurement "repetitions", :category #{:core}}
                    {:next ["Kravle på albuer og føtter"],
                     :previous ["Kravle på hender og knær"],
                     :image "elbow-knee-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Kravle på albuer og knær"}
                    {:next ["Push-up bro ett ben"],
                     :previous ["Push-up"],
                     :image "bridge-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength :bridge},
                     :name "Push-up bro"}
                    {:next ["Assistert en arm pull-up"],
                     :previous ["Pull-up til bryst" "Pull-up og strekke seg"],
                     :image "archer-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :pull-up :pull},
                     :name "Bueskytter pull-up"}
                    {:name "Sprinte", :image "sprint.png", :measurement "distance", :category #{:run :natural}}
                    {:previous ["Svinge sideveis slipp"],
                     :image "lateral-swing-holds.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :brachiate :hang},
                     :name "Svinge sideveis hold"}
                    {:next ["Sideveis hopp med tilløp"],
                     :previous ["Presisjonshopp"],
                     :image "running-jump.png",
                     :measurement "repetitions",
                     :category #{:natural :jump},
                     :name "Hopp med tilløp"}
                    {:previous ["Negativ muscle-up i ringer"],
                     :image "ring-muscle-up.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :muscle-up},
                     :name "Muscle-up i ringer"}
                    {:next ["Pull-up"],
                     :image "jumping-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :natural :climb :pull-up :pull},
                     :name "Pull-up med hopp"}
                    {:previous ["Galopperende kravle"],
                     :image "traveling-squat.png",
                     :measurement "distance",
                     :category #{:crawl},
                     :name "Knebøyekspressen"}
                    {:name "Foroverrulle", :image "forward-roll.png", :measurement "repetitions", :category #{:natural :roll}}
                    {:next ["Reke med kneet"],
                     :image "shin-shrimp-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg :balance},
                     :name "Reke med leggen"}
                    {:previous ["Henge i falskt grep" "Chin-up i ringer"],
                     :image "false-grip-chin-up.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :pull-up :pull},
                     :name "Falskt grep chin-up"}
                    {:previous ["Straddle back-lever"],
                     :image "back-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :bar :back-lever},
                     :name "Back-lever"}
                    {:next ["Air baby strekk"],
                     :image "frog-stance.png",
                     :measurement "duration",
                     :category #{:wrist-mobility :air-baby},
                     :name "Frosk"}
                    {:name "Strutsegange", :image "ostrich-walk.png", :measurement "distance", :category #{:mobility}}
                    {:name "Hofterotasjoner på gulvet",
                     :image "floor-hip-rotation.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:next ["Muscle-up i ringer"],
                     :previous ["Muscle-up overgang" "Falskt grep chin-up" "Dip i ringer"],
                     :image "negative-ring-muscle-up.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :muscle-up},
                     :name "Negativ muscle-up i ringer"}
                    {:next ["Straddle side-lever"],
                     :previous ["Tuck side-lever"],
                     :image "flat-side-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :side-lever :bar},
                     :name "Flat side-lever"}
                    {:name "Knebøy til knele",
                     :image "squat-to-kneeling.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :natural :sit-stand-transition :mobility :ankle-mobility}}
                    {:previous ["Henge aktivt med en arm"],
                     :image "one-arm-pull.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :hang},
                     :name "Henge passivt til aktivt med en arm"}
                    {:previous ["Hollow body"],
                     :image "hollow-body-rock.png",
                     :measurement "repetitions",
                     :category #{:core},
                     :name "Hollow body gynge"}
                    {:name "Tricep dip", :image "tricep-dip.png", :measurement "repetitions", :category #{:push :dip :bent-arm-strength}}
                    {:previous ["Henge aktivt med en arm"],
                     :image "lateral-swing.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :brachiate :hang},
                     :name "Svinge sideveis"}
                    {:next ["Balanserende sideveis sniking"],
                     :previous ["Lav balansegang"],
                     :image "stealth-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Balanserende sniking"}
                    {:previous ["Spark til håndstående"],
                     :image "handstand.png",
                     :measurement "duration",
                     :category #{:shoulder-mobility :wrist-mobility :handstand},
                     :name "Håndstående"}
                    {:next ["Negativ en arm pull-up"],
                     :previous ["Chin-up i ringer"],
                     :image "assisted-one-arm-pull-up.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :pull-up :pull},
                     :name "Assistert pull-up i en arm"}
                    {:next ["Rekekravle"],
                     :image "shoulder-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Skulderkravle"}
                    {:next ["Rotasjon inn i høy bro"],
                     :image "high-bridge.png",
                     :measurement "duration",
                     :category #{:shoulder-mobility :wrist-mobility :bridge :spine-mobility :mobility},
                     :name "Høy bro"}
                    {:next ["Muscle-up"],
                     :previous ["Svingende pop-up"],
                     :image "tuck-pop-up.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb},
                     :name "Eksplosiv pop-up"}
                    {:next ["Strekk ben front-lever"],
                     :previous ["Tuck front-lever"],
                     :image "flat-front-lever.png",
                     :measurement "duration",
                     :category #{:ring :front-lever :straight-arm-strength :bar},
                     :name "Flat front-lever"}
                    {:next ["Vid pull-up"],
                     :previous ["Ro"],
                     :image "chin-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :pull-up :pull},
                     :name "Chin-up"}
                    {:name "Knebøy splitt",
                     :image "split-squat.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :natural :sit-stand-transition :mobility}}
                    {:previous ["Pull-up og strekke seg"],
                     :image "explosive-pull-up.png",
                     :measurement "repetition",
                     :category #{:bar :bent-arm-strength :natural :climb :pull-up :pull},
                     :name "Eksplosiv pull-up"}
                    {:name "Sideveis halv knele",
                     :image "lateral-half-kneeling.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility :ankle-mobility}}
                    {:name "Ro i ringer", :image "ring-row.png", :measurement "repetitions", :category #{:ring :bent-arm-strength :pull}}
                    {:next ["Balanserende vending i splittknebøy"],
                     :image "balancing-cross-turn.png",
                     :measurement "repetitions",
                     :category #{:natural :balance :beam},
                     :name "Balanserende kryssvending"}
                    {:name "Bære ved hofta", :image "waist-carry.png", :measurement "distance", :category #{:natural :weight :carry}}
                    {:name "Bre seg bakover",
                     :image "backward-sprawl.png",
                     :measurement "repetitions",
                     :category #{:natural :sit-stand-transition}}
                    {:name "Sitte på huk",
                     :image "deep-knee-bend.png",
                     :measurement "duration",
                     :category #{:hip-mobility :natural :sit-stand-transition :mobility}}
                    {:name "Beinsving bakover",
                     :image "back-leg-swing.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:next ["Tuck planche"],
                     :previous ["Planche len"],
                     :image "handstand-lean.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :planche},
                     :name "Håndstående len"}
                    {:next ["Svinge seg opp med albuene"],
                     :previous ["Hengende fotkrok"],
                     :image "sliding-swing-up.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb},
                     :name "Svinge seg opp"}
                    {:next ["Negativ muscle-up i ringer"],
                     :image "ring-muscle-up-transition.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :muscle-up},
                     :name "Muscle-up overgang"}
                    {:name "Scapula push-up",
                     :image "scapula-push-up.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :straight-arm-strength :mobility}}
                    {:next ["Håndstående push-up"],
                     :previous ["Vegg håndstående push-up"],
                     :image "ring-handstand-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Håndstående push-up i ringer"}
                    {:previous ["Henge passivt til aktivt"],
                     :image "arching-hang.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :hang},
                     :name "Henge i bue"}
                    {:next ["Pull-up til bryst"],
                     :previous ["Chin-up"],
                     :image "wide-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :pull-up :pull},
                     :name "Vid pull-up"}
                    {:name "Frontutfall",
                     :image "front-lunge.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :lunge :natural :weight}}
                    {:next ["Bjørnekravle baklengs"],
                     :previous ["Kravle på albuer og føtter"],
                     :image "bear-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Bjørnekravle"}
                    {:next ["Push-up bro en arm"],
                     :previous ["Push-up bro"],
                     :image "one-leg-bridge-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength :bridge},
                     :name "Push-up bro ett ben"}
                    {:next ["Hengende sideløft"],
                     :previous ["Hengende kneløft"],
                     :image "toes-to-bar.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb :leg-lift},
                     :name "Tærne til stanga"}
                    {:name "Flies med strikk over hodet",
                     :image "band-overhead-fly.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:name "Skuldervridning med strikk",
                     :image "band-shoulder-dislocation.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:name "Thruster",
                     :image "thruster.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :push :squat :weight}}
                    {:next ["Russisk dip" "Dip på enkel stang"],
                     :previous ["Dip med hopp" "Negativ dip"],
                     :image "dip.png",
                     :measurement "repetitions",
                     :category #{:push :dip :bar :bent-arm-strength :natural :climb},
                     :name "Dip"}
                    {:next ["Henge passivt til aktivt med en arm"],
                     :previous ["Henge passivt med en arm"],
                     :image "one-arm-active-hang.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :hang},
                     :name "Henge aktivt med en arm"}
                    {:previous ["Chin-up i ringer"],
                     :image "wide-ring-chin-up.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :pull-up :pull},
                     :name "Vid chin-up i ringer"}
                    {:previous ["Dip på enkel stang" "Eksplosiv pull-up" "Negativ muscle-up"],
                     :image "muscle-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :natural :climb :muscle-up},
                     :name "Muscle-up"}
                    {:name "Liggende bensirkler", :image "lying-leg-circle.png", :measurement "repetitions", :category #{:core}}
                    {:next ["Krabbekravle baklengs"],
                     :previous ["Krabbekravle"],
                     :image "backward-inverted-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Krabbekravle baklengs"}
                    {:previous ["Negativ en arm pull-up"],
                     :image "one-arm-pull-up.png",
                     :measurement "repetitions",
                     :category #{:ring :bent-arm-strength :pull-up :pull},
                     :name "En arm pull-up"}
                    {:previous ["L-sit"],
                     :image "ring-l-sit.png",
                     :measurement "duration",
                     :category #{:l-sit :core},
                     :name "L-sit i ringer"}
                    {:name "Dynamisk tåstrekk",
                     :image "dynamic-toes-stretch.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:previous ["Push-up"],
                     :image "headstand-push-out.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Dytt ut fra hodestående"}
                    {:previous ["Pistol med stol"],
                     :image "pistol-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :single-leg :balance},
                     :name "Pistol"}
                    {:next ["Side-lever"],
                     :previous ["Flat side-lever"],
                     :image "straddle-side-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :side-lever :bar},
                     :name "Straddle side-lever"}
                    {:previous ["Dip"],
                     :image "russian-dip.png",
                     :measurement "repetitions",
                     :category #{:push :dip :bar :bent-arm-strength :natural :climb},
                     :name "Russisk dip"}
                    {:next ["Planche"],
                     :previous ["Strekk ben planche"],
                     :image "straddle-planche.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :planche},
                     :name "Straddle planche"}
                    {:previous ["Henge passivt"],
                     :image "false-grip-hang.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :hang},
                     :name "Henge med falskt grep"}
                    {:name "Heve sideveis med strikk",
                     :image "band-lateral-raise.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:next ["Clean"],
                     :previous ["Power clean fra hofta"],
                     :image "power-clean.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :natural :weight},
                     :name "Power clean"}
                    {:previous ["Pull-up" "Svinge sideveis hold"],
                     :image "bent-arm-linear-traverse.png",
                     :measurement "distance",
                     :category #{:bar :bent-arm-strength :natural :brachiate :hang},
                     :name "Traversere"}
                    {:next ["Sit-up brodd"], :image "sit-up.png", :measurement "repetitions", :category #{:core}, :name "Sit-up"}
                    {:name "Sideveis shuffle", :image "side-shuffle.png", :measurement "distance", :category #{:walk :natural}}
                    {:name "Tauklatring", :image "rope-climb.png", :measurement "distance", :category #{:rope :natural :climb}}
                    {:next ["Straddle front-lever"],
                     :previous ["Flat front-lever"],
                     :image "one-leg-extended-front-lever.png",
                     :measurement "duration",
                     :category #{:ring :front-lever :straight-arm-strength :bar},
                     :name "Strekk ben front-lever"}
                    {:previous ["Push-up"],
                     :image "russian-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Russisk push-up"}
                    {:name "Rotasjon med strikk",
                     :image "band-side-rotation.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:next ["Lav balansegang"],
                     :previous ["Balansegang"],
                     :image "balancing-backward-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Balansere baklengs"}
                    {:next ["L-sit på stang"],
                     :image "parallell-bar-tuck-l-sit.png",
                     :measurement "duration",
                     :category #{:l-sit :core},
                     :name "Tuck l-sit på stang"}
                    {:previous ["Clean fra hofta"],
                     :image "clean.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :natural :weight},
                     :name "Clean"}
                    {:next ["Dip"],
                     :image "jumping-dip.png",
                     :measurement "repetitions",
                     :category #{:push :dip :bar :bent-arm-strength :natural :climb},
                     :name "Dip med hopp"}
                    {:previous ["Vid dip i ringer"],
                     :image "archer-ring-dip.png",
                     :measurement "repetitions",
                     :category #{:push :ring :dip :bent-arm-strength},
                     :name "Bueskytter dip i ringer"}
                    {:name "Kaste over hodet", :image "overhead-throw.png", :measurement "repetitions", :category #{:natural :throw}}
                    {:name "Knele til halv knele",
                     :image "kneeling-to-tall-half-kneeling.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :sit-stand-transition :mobility}}
                    {:name "Gå over",
                     :image "stepping-over.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :walk :natural :mobility}}
                    {:previous ["Spark til håndstående"],
                     :image "backward-roll-to-handstand.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :handstand},
                     :name "Rulle baklengs til håndstående"}
                    {:next ["Flat back-lever"],
                     :image "tuck-back-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :bar :back-lever},
                     :name "Tuck back-lever"}
                    {:previous ["Presisjonshopp"],
                     :image "depth-jump.png",
                     :measurement "repetitions",
                     :category #{:natural :jump},
                     :name "Hopp ned"}
                    {:image "side-plank.png",
                     :measurement "duration",
                     :category #{:side-lever :core},
                     :name "Sideplanke",
                     :mext ["Tuck side-lever"]}
                    {:name "Gå på veggen",
                     :image "wall-walk.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :handstand}}
                    {:name "Bro push-up",
                     :image "bridge-push-up.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :bridge :spine-mobility :mobility}}
                    {:name "Vektet utfall",
                     :image "back-lunge.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :lunge :weight}}
                    {:name "Lav rotasjon med strikk",
                     :image "band-low-rotation.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:previous ["Liggende benløft"],
                     :image "sitting-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:l-sit :core},
                     :name "Sittende benløft"}
                    {:name "Knebøy hofterotasjon",
                     :image "squat-hip-rotation.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:next ["Strekk ben back-lever"],
                     :previous ["Tuck back-lever"],
                     :image "flat-back-lever.png",
                     :measurement "duration",
                     :category #{:ring :straight-arm-strength :bar :back-lever},
                     :name "Flat back-lever"}
                    {:next ["Vid pull-up" "Pull-up og strekke seg"],
                     :previous ["Negativ pull-up" "Pull-up med hopp"],
                     :image "pull-up.png",
                     :measurement "repetition",
                     :category #{:bar :bent-arm-strength :natural :climb :pull-up :pull},
                     :name "Pull-up"}
                    {:name "Kaste med rotasjon", :image "rotational-throw.png", :measurement "repetitions", :category #{:natural :throw}}
                    {:next ["Negativ en arm pull-up"],
                     :previous ["Bueskytter pull-up"],
                     :image "assisted-one-arm-pull-up.png",
                     :measurement "repetitions",
                     :category #{:bar :bent-arm-strength :pull-up :pull},
                     :name "Assistert en arm pull-up"}
                    {:name "Kast", :image "chest-throw.png", :measurement "repetitions", :category #{:natural :throw}}
                    {:next ["Power clean"],
                     :previous ["Markløft"],
                     :image "hang-power-clean.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :natural :weight},
                     :name "Power clean fra hofta"}
                    {:next ["Eksplosiv pull-up"],
                     :previous ["Pull-up"],
                     :image "pull-up-reach.png",
                     :measurement "repetition",
                     :category #{:bar :bent-arm-strength :natural :climb :pull-up :pull},
                     :name "Pull-up og strekke seg"}
                    {:previous ["Håndstående push-up med spark"],
                     :image "handstand-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Håndstående push-up"}
                    {:previous ["Spark til håndstående"],
                     :image "handstand-walk.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :handstand :acrobatic},
                     :name "Gå på hendene"}
                    {:previous ["Push-up"],
                     :image "pseudo-planche-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Pseudo-planche push-up"}
                    {:next ["Spiderman Crawl"],
                     :image "army-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Militærkravle"}
                    {:previous ["Krabbekravle baklengs"],
                     :image "lateral-inverted-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Krabbekravle sideveis"}
                    {:name "Hollow body", :image "hollow-body.png", :measurement "duration", :category #{:core}}
                    {:next ["Clean"],
                     :previous ["Frontbøy" "Power clean fra hofta"],
                     :image "hang-clean.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :natural :weight},
                     :name "Clean fra hofta"}
                    {:name "Bakoverrulle til liggende",
                     :image "backward-roll-to-lying.png",
                     :measurement "repetitions",
                     :category #{:natural :roll}}
                    {:previous ["Markløft"],
                     :image "lapping.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :natural :weight},
                     :name "Fang"}
                    {:name "Rotasjon med strikk over hodet",
                     :image "band-overhead-rotation.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :elastic-band :mobility}}
                    {:previous ["Høy bro"],
                     :image "bridge-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :bridge :spine-mobility},
                     :name "Gå i bro"}
                    {:name "Splitt knele til sitte",
                     :image "split-kneeling-to-sitting.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :natural :sit-stand-transition :mobility}}
                    {:next ["Henge i bue" "Pull-up fra albuene"],
                     :previous ["Henge aktivt"],
                     :image "hanging-pull.png",
                     :measurement "repetitions",
                     :category #{:straight-arm-strength :bar :natural :hang},
                     :name "Henge passivt til aktivt"}
                    {:name "4-veis ryggradstøying",
                     :image "four-way-spine-stretch.png",
                     :measurement "repetitions",
                     :category #{:spine-mobility :mobility}}
                    {:previous ["Push-up"],
                     :image "wide-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Vid push-up"}
                    {:previous ["Push-up bro ett ben"],
                     :image "one-arm-bridge-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength :bridge},
                     :name "Push-up bro en arm"}
                    {:name "Hengslet gange", :image "hinged-walk.png", :measurement "distance", :category #{:walk :natural}}
                    {:next ["V-up"], :image "tuck-v-up.png", :measurement "repetitions", :category #{:core}, :name "Tuck v-up"}
                    {:next ["Bueskytter dip i ringer"],
                     :previous ["Dip i ringer"],
                     :image "wide-ring-dip.png",
                     :measurement "repetitions",
                     :category #{:push :ring :dip :bent-arm-strength},
                     :name "Vid dip i ringer"}
                    {:name "Bære ved brystet", :image "chest-carry.png", :measurement "distance", :category #{:natural :weight :carry}}
                    {:next ["Hopp opp" "Hopp ned" "Hopp med tilløp"],
                     :previous ["Svinghopp"],
                     :image "broad-jump.png",
                     :measurement "repetitions",
                     :category #{:natural :jump},
                     :name "Presisjonshopp"}
                    {:next ["Push-up en arm ett ben"],
                     :previous ["Bueskytter push-up"],
                     :image "one-arm-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Push-up en arm"}
                    {:name "Slå hjul",
                     :image "cartwheel.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :handstand :acrobatic}}
                    {:previous ["Push-press"],
                     :image "jerk.png",
                     :measurement "repetitions",
                     :category #{:lift :push :natural :weight},
                     :name "Støt"}
                    {:name "Supermann", :image "superman.png", :measurement "duration", :category #{:core}}
                    {:next ["Balanserende vending i splittknebøy"],
                     :image "balancing-turn.png",
                     :measurement "repetitions",
                     :category #{:natural :balance :beam},
                     :name "Balanserende vending"}
                    {:name "Tøye håndledd",
                     :image "wrist-stretches.png",
                     :measurement "repetitions",
                     :category #{:wrist-mobility :mobility}}
                    {:name "Bre seg forover til push-up",
                     :image "forward-sprawl-to-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength :natural}}
                    {:next ["Doble hopp"],
                     :image "single-under.png",
                     :measurement "repetitions",
                     :category #{:footwork :jump-rope},
                     :name "Enkle hopp"}
                    {:next ["Gå på hendene"],
                     :previous ["Galopperende kravle"],
                     :image "hover-crawl.png",
                     :measurement "distance",
                     :category #{:crawl},
                     :name "Svevende kravle"}
                    {:previous ["Spark til håndstående"],
                     :image "one-arm-wall-handstand.png",
                     :measurement "duration",
                     :category #{:shoulder-mobility :wrist-mobility :handstand},
                     :name "Håndstående mot vegg på en arm"}
                    {:next ["Bjørnekravle"],
                     :previous ["Kravle på albuer og knær"],
                     :image "elbow-foot-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Kravle på albuer og føtter"}
                    {:name "Gå under",
                     :image "stepping-under.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :walk :natural :mobility}}
                    {:next ["Presisjonshopp"],
                     :image "single-leg-jump.png",
                     :measurement "repetitions",
                     :category #{:natural :jump},
                     :name "Svinghopp"}
                    {:next ["Balansere sideveis"
                            "Balansere baklengs"
                            "Lav balansegang"
                            "Balanserende bjørnekravle"
                            "Balanserende krabbekravle"],
                     :image "balancing-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Balansegang"}
                    {:next ["Lav balansegang"],
                     :previous ["Balansegang"],
                     :image "balancing-lateral-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Balansere sideveis"}
                    {:name "Svinge",
                     :image "linear-swing.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :natural :brachiate :hang}}
                    {:next ["Henge aktivt"],
                     :image "passive-hang.png",
                     :measurement "duration",
                     :category #{:bar :natural :hang},
                     :name "Henge passivt"}
                    {:name "Over hodet knebøy",
                     :image "overhead-squat.png",
                     :measurement "repetitions",
                     :category #{:leg-strength :lift :squat :weight}}
                    {:previous ["Straddle front-lever"],
                     :image "front-lever.png",
                     :measurement "duration",
                     :category #{:ring :front-lever :straight-arm-strength :bar},
                     :name "Front-lever"}
                    {:next ["Håndstående push-up"],
                     :previous ["Vegg håndstående push-up" "Spark til håndstående"],
                     :image "handstand-push-up-kip.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :shoulder-mobility :bent-arm-strength :wrist-mobility :handstand},
                     :name "Håndstående push-up med spark"}
                    {:name "Dynamisk soleus-strekk",
                     :image "dynamic-soleus-stretch.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:previous ["Push-up en arm"],
                     :image "one-leg-one-arm-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Push-up en arm ett ben"}
                    {:name "Jogge", :image "run.png", :measurement "distance", :category #{:run :natural}}
                    {:name "Beinsving forover",
                     :image "front-leg-swing.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:name "Rykk", :image "snatch.png", :measurement "repetitions", :category #{:leg-strength :lift :squat :weight}}
                    {:name "Knebøy strekk mot taket",
                     :image "squat-sky-reach.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :spine-mobility :mobility}}
                    {:name "Dytt kneet vekk",
                     :image "knee-push-away.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :mobility}}
                    {:name "Knebøy til høy knele",
                     :image "squat-to-tall-kneeling.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :natural :sit-stand-transition :mobility :ankle-mobility}}
                    {:name "Ande-gange", :image "duck-walk.png", :measurement "distance", :category #{:hip-mobility :walk :mobility}}
                    {:previous ["Overgang"],
                     :image "balancing-tripod-transition.png",
                     :measurement "repetitions",
                     :category #{:natural :balance :beam},
                     :name "Balanserende overgang"}
                    {:previous ["Dip på enkel stang"],
                     :image "korean-dip.png",
                     :measurement "repetitions",
                     :category #{:push :dip :bar :bent-arm-strength},
                     :name "Koreansk dip"}
                    {:previous ["Push-up"],
                     :image "ring-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :ring :bent-arm-strength},
                     :name "Push-up i ringer"}
                    {:name "Knebøy hvile",
                     :image "resting-squat.png",
                     :measurement "duration",
                     :category #{:hip-mobility :natural :sit-stand-transition :mobility :ankle-mobility}}
                    {:previous ["Straddle planche"],
                     :image "planche.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :planche},
                     :name "Planche"}
                    {:previous ["Hengende 90 benløft"],
                     :image "hanging-90-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:l-sit :core},
                     :name "Hengende benløft i en arm"}
                    {:next ["Pull-up fra albuene"],
                     :previous ["Svinge seg opp med albuene"],
                     :image "hand-swing-up.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb},
                     :name "Svinge seg opp med hendene"}
                    {:previous ["Henge aktivt med en arm" "Svinge sideveis hold"],
                     :image "figure-8-swing.png",
                     :measurement "duration",
                     :category #{:straight-arm-strength :bar :brachiate :hang},
                     :name "Svinge i figur 8"}
                    {:previous ["Hodestående benløft"],
                     :image "headstand-straight-leg-lift.png",
                     :measurement "repetitions",
                     :category #{:core},
                     :name "Hodestående rett benløft"}
                    {:previous ["Rotasjon mot veggen" "Skli inn i lav bro"],
                     :image "rotate-into-low-bridge.png",
                     :measurement "repetitions",
                     :category #{:shoulder-mobility :wrist-mobility :bridge :spine-mobility :mobility},
                     :name "Rotasjon inn i lav bro"}
                    {:previous ["Presisjonshopp"],
                     :image "box-jump.png",
                     :measurement "repetitions",
                     :category #{:natural :jump},
                     :name "Hopp opp"}
                    {:next ["Hevet push-up i vinkel"],
                     :previous ["Hindu push-up"],
                     :image "pike-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Push-up i vinkel"}
                    {:next ["Eksplosiv pop-up"],
                     :previous ["Pull-up fra albuene"],
                     :image "swing-pop-up.png",
                     :measurement "repetitions",
                     :category #{:bar :natural :climb},
                     :name "Svingende pop-up"}
                    {:previous ["Skulderkravle"],
                     :image "hip-thrust-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :natural},
                     :name "Rekekravle"}
                    {:previous ["Lav balansegang"],
                     :image "balancing-backward-low-walk.png",
                     :measurement "distance",
                     :category #{:walk :natural :balance :balancing-locomotion :beam},
                     :name "Balansere lavt baklengs"}
                    {:name "Invertert planke", :image "inverted-plank.png", :measurement "duration", :category #{:core}}
                    {:next ["Øglekravle"],
                     :previous ["Spiderman-kravle"],
                     :image "lizard-crawl.png",
                     :measurement "distance",
                     :category #{:crawl :bent-arm-strength},
                     :name "Øglekravle"}
                    {:name "Knebøy bukk",
                     :image "squat-bow.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :spine-mobility :mobility}}
                    {:name "Sitte til halv knele",
                     :image "sit-to-tall-half-kneeling.png",
                     :measurement "repetitions",
                     :category #{:hip-mobility :sit-stand-transition :mobility}}
                    {:next ["Vegg håndstående push-up"],
                     :previous ["Hevet push-up i vinkel"],
                     :image "wall-kip-handstand-push-up.png",
                     :measurement "repetitions",
                     :category #{:push-up :push :bent-arm-strength},
                     :name "Vegg håndstående push-up med spark"}
                    {:name "Fot-kne-gange",
                     :image "split-kneeling-walk.png",
                     :measurement "distance",
                     :category #{:hip-mobility :walk :natural :mobility}}
                    {:name "Henge i falskt grep", :image "false-grip-hang.png", :measurement "duration", :category #{:wrist-mobility}}
                    {:name "Kravle som måler", :image "inchworm.png", :measurement "distance", :category #{:crawl}}
                    {:name "Tripod-overgang",
                     :image "tripod-transition.png",
                     :measurement "repetitions",
                     :category #{:natural :sit-stand-transition :mobility}}
                    {:next ["L-sit i ringer"],
                     :previous ["L-sit"],
                     :image "l-straddle.png",
                     :measurement "duration",
                     :category #{:l-sit :core},
                     :name "L-straddle"}
                    {:next ["L-straddle"],
                     :previous ["L-sit på stang"],
                     :image "l-sit.png",
                     :measurement "duration",
                     :category #{:l-sit :core},
                     :name "L-sit"}
                    {:name "Tåstrekk", :image "toes-stretch.png", :measurement "duration", :category #{:hip-mobility :mobility}}])

(def templates [{:parts [[{:set 5N, :movement "Hopp opp", :natural-only? true, :slot-category #{:jump}, :rep 1N}
                          {:set 5N, :movement "Hopp ned", :natural-only? true, :slot-category #{:jump}, :rep 1N}
                          {:distance 10N, :set 5N, :natural-only? true, :slot-category #{:balance}, :rep 6N}
                          {:duration 20N, :set 5N, :natural-only? true, :slot-category #{:climb}, :rep 8N}
                          {:distance 10N, :set 5N, :natural-only? true, :slot-category #{:crawl}}]],
                 :title "Naturlige Bevegelser 3",
                 :description ["Gå gjennom 4-6 runder av disse øvelsene. Finn et tempo så du ikke behøver pauser."]}
                {:title "Gymnastic Strength 2",
                 :parts [[{:duration 10N, :set 4N, :slot-category #{:planche :back-lever}, :rest 10N}
                          {:set 4N, :slot-category #{:push}, :rest 60N, :rep 5N}]
                         [{:duration 10N, :set 4N, :slot-category #{:front-lever :side-lever}, :rest 10N}
                          {:set 4N, :slot-category #{:pull}, :rest 60N, :rep 5N}]
                         [{:duration 10N, :set 4N, :slot-category #{:l-sit}, :rest 10N, :rep 10N}
                          {:set 4N, :slot-category #{:single-leg :jump}, :rest 60N, :rep 6N}]]}
                {:title "Mobility 1",
                 :parts [[{:set 1N, :movement "Dynamisk ankelfleksjon", :slot-category #{:ankle-mobility}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 2N, :slot-category #{:hip-mobility :ankle-mobility}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 2N, :slot-category #{:hip-mobility :ankle-mobility}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 2N, :slot-category #{:hip-mobility :ankle-mobility}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 2N, :slot-category #{:hip-mobility :ankle-mobility}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 2N, :slot-category #{:hip-mobility :ankle-mobility}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 2N, :slot-category #{:hip-mobility :ankle-mobility}, :rep 10N}]
                         [{:set 1N, :movement "Tøye håndledd", :slot-category #{:wrist-mobility}, :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 2N,
                           :slot-category #{:shoulder-mobility :wrist-mobility :spine-mobility},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 2N,
                           :slot-category #{:shoulder-mobility :wrist-mobility :spine-mobility},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 2N,
                           :slot-category #{:shoulder-mobility :wrist-mobility :spine-mobility},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 2N,
                           :slot-category #{:shoulder-mobility :wrist-mobility :spine-mobility},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 2N,
                           :slot-category #{:shoulder-mobility :wrist-mobility :spine-mobility},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 2N,
                           :slot-category #{:shoulder-mobility :wrist-mobility :spine-mobility},
                           :rep 10N}]]}
                {:parts [[{:duration 30N,
                           :distance 15N,
                           :set 3N,
                           :natural-only? true,
                           :slot-category #{:crawl :sit-stand-transition :balance :mobility :roll},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 3N,
                           :natural-only? true,
                           :slot-category #{:crawl :sit-stand-transition :balance :mobility :roll},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 3N,
                           :natural-only? true,
                           :slot-category #{:crawl :sit-stand-transition :balance :mobility :roll},
                           :rep 10N}
                          {:duration 30N,
                           :distance 15N,
                           :set 3N,
                           :natural-only? true,
                           :slot-category #{:crawl :sit-stand-transition :balance :mobility :roll},
                           :rep 10N}]
                         [{:duration 240N,
                           :distance 50N,
                           :set 1N,
                           :natural-only? true,
                           :slot-category #{:lift :run :crawl :sit-stand-transition :balance :mobility :roll},
                           :rep 50N}]
                         [{:duration 30N, :distance 15N, :set 4N, :natural-only? true, :slot-category #{:natural}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 4N, :natural-only? true, :slot-category #{:natural}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 4N, :natural-only? true, :slot-category #{:natural}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 4N, :natural-only? true, :slot-category #{:natural}, :rep 10N}
                          {:duration 30N, :distance 15N, :set 4N, :natural-only? true, :slot-category #{:natural}, :rep 10N}]],
                 :title "Naturlige Bevegelser 1",
                 :description ["Varm opp med mobilitetsøvelser. Velg så en øvelse å trene teknikk på. Avslutt med 4-6 runder sirkeltrening uten pauser."
                               "Oppvarming, teknikktrening og svetting!"]}
                {:title "Gymnastic Strength 1",
                 :parts [[{:duration 30N, :set 4N, :slot-category #{:straight-arm-strength}, :rest 60N, :rep 10N}
                          {:duration 30N, :set 4N, :slot-category #{:straight-arm-strength}, :rest 60N, :rep 10N}]
                         [{:set 3N, :slot-category #{:bent-arm-strength}, :rest 60N, :rep 5N}
                          {:set 3N, :slot-category #{:bent-arm-strength}, :rest 60N, :rep 5N}
                          {:set 3N, :slot-category #{:bent-arm-strength}, :rest 60N, :rep 5N}]
                         [{:set 3N, :slot-category #{:single-leg :jump}, :rest 60N, :rep 10N}
                          {:set 3N, :slot-category #{:single-leg :jump}, :rest 60N, :rep 10N}
                          {:set 3N, :slot-category #{:single-leg :jump}, :rest 60N, :rep 10N}]
                         [{:duration 60N, :set 2N, :slot-category #{:core}, :rest 30N, :rep 10N}
                          {:duration 60N, :set 2N, :slot-category #{:core}, :rest 30N, :rep 10N}]]}
                {:title "Naturlige Bevegelser 4",
                 :parts [[{:duration 15N, :distance 6N, :set 5N, :natural-only? true, :slot-category #{:brachiate}}
                          {:distance 10N, :set 5N, :natural-only? true, :slot-category #{:balance}}
                          {:set 5N, :natural-only? true, :slot-category #{:sit-stand-transition}, :rep 8N}
                          {:set 5N, :natural-only? true, :slot-category #{:jump}, :rep 8N}
                          {:distance 20N, :set 5N, :natural-only? true, :slot-category #{:balance}}]]}
                {:title "Mobility 3",
                 :parts [[{:set 1N, :movement "Dynamisk soleus-strekk", :slot-category #{:hip-mobility}, :rep 10N}
                          {:set 1N, :movement "Hofterotasjoner på gulvet", :slot-category #{:hip-mobility}, :rep 10N}
                          {:set 1N, :movement "Knebøy hofterotasjon", :slot-category #{:hip-mobility}, :rep 10N}
                          {:set 1N, :movement "Tåstrekk", :slot-category #{:hip-mobility}, :rep 10N}
                          {:set 1N, :movement "Dynamisk tåstrekk", :slot-category #{:hip-mobility}, :rep 10N}
                          {:set 1N, :movement "Knebøy hvile", :slot-category #{:hip-mobility}, :rep 10N}
                          {:set 1N, :movement "Knebøy", :slot-category #{:hip-mobility}, :rep 10N}]]}
                {:title "Locomotion 1",
                 :parts [[{:set 1N, :movement "Tøye håndledd", :slot-category #{:wrist-mobility}, :rep 10N}
                          {:set 1N, :movement "Dynamisk ankelfleksjon", :slot-category #{:ankle-mobility}, :rep 10N}
                          {:set 1N, :slot-category #{:mobility}, :rep 10N}
                          {:set 1N, :slot-category #{:mobility}, :rep 10N}
                          {:set 1N, :slot-category #{:mobility}, :rep 10N}
                          {:set 1N, :slot-category #{:mobility}, :rep 10N}]
                         [{:distance 15N, :set 4N, :slot-category #{:walk :crawl}, :rep 10N}
                          {:distance 15N, :set 4N, :slot-category #{:walk :crawl}, :rep 10N}
                          {:distance 15N, :set 4N, :slot-category #{:walk :crawl}, :rep 10N}
                          {:distance 15N, :set 4N, :slot-category #{:walk :crawl}, :rep 10N}
                          {:distance 15N, :set 4N, :slot-category #{:walk :crawl}, :rep 10N}
                          {:distance 15N, :set 4N, :slot-category #{:walk :crawl}, :rep 10N}]]}
                {:title "Mobility 2",
                 :parts [[{:set 1N, :movement "Dytt kneet vekk", :slot-category #{:hip-mobility}, :rep 20N}
                          {:set 1N, :movement "Knebøy strekk mot taket", :slot-category #{:hip-mobility}, :rep 20N}
                          {:duration 20N, :set 1N, :movement "Knebøy hold mot taket", :slot-category #{:hip-mobility}}
                          {:set 1N, :movement "Knebøy bønn", :slot-category #{:hip-mobility}, :rep 20N}
                          {:set 1N, :movement "Knebøy bukk", :slot-category #{:hip-mobility}, :rep 20N}]]}
                {:parts [[{:duration 30N,
                           :distance 12N,
                           :set 4N,
                           :natural-only? true,
                           :slot-category #{:crawl :sit-stand-transition},
                           :rep 8N}
                          {:duration 30N,
                           :distance 12N,
                           :set 4N,
                           :natural-only? true,
                           :slot-category #{:crawl :sit-stand-transition},
                           :rep 8N}
                          {:duration 30N,
                           :distance 12N,
                           :set 4N,
                           :natural-only? true,
                           :slot-category #{:crawl :sit-stand-transition},
                           :rep 8N}
                          {:set 4N, :natural-only? true, :slot-category #{:jump}, :rep 4N}
                          {:set 4N, :natural-only? true, :slot-category #{:jump}, :rep 4N}
                          {:duration 30N, :set 4N, :natural-only? true, :slot-category #{:hang}, :rep 1N}]],
                 :title "Naturlige Bevegelser 2",
                 :description ["Gjør 4+ runder i et jevnt tempo"]}
                {:title "Mobility 4",
                 :parts [[{:set 1N, :movement "Utvendig rotasjon med strikk", :slot-category #{:shoulder-mobility}, :rep 15N}
                          {:set 1N, :movement "Heve sideveis med strikk", :slot-category #{:shoulder-mobility}, :rep 15N}
                          {:set 1N, :movement "Rotasjon med strikk over hodet", :slot-category #{:shoulder-mobility}, :rep 10N}
                          {:set 1N, :movement "Rotasjon med strikk", :slot-category #{:shoulder-mobility}, :rep 10N}
                          {:set 1N, :movement "Lav rotasjon med strikk", :slot-category #{:shoulder-mobility}, :rep 10N}
                          {:set 1N, :movement "Flies med strikk over hodet", :slot-category #{:shoulder-mobility}, :rep 10N}
                          {:set 1N, :movement "Flies med strikk", :slot-category #{:shoulder-mobility}, :rep 10N}
                          {:set 1N, :movement "Lave flies med strikk", :slot-category #{:shoulder-mobility}, :rep 10N}]]}]
  )