package com.example.badgeuse_auto.ui.help

object BadgeHelp {

    /* ================= MODES DE BADGEAGE ================= */

    const val OFFICE = """
Mode multi-lieux classique.

Chaque lieu est géré indépendamment.

Fonctionnement :
• Entrée automatique à l’arrivée sur un lieu
• Sortie automatique au départ du lieu
• Plusieurs lieux possibles dans la même journée
• Les temps sont calculés séparément pour chaque lieu

Recommandé pour :
• Bureaux
• Interventions multiples
• Déplacements entre sites
"""

    const val DEPOT = """
Mode dépôt / chantier (journée unique).

Fonctionnement :
• Une seule journée de travail
• L’entrée correspond à la PREMIÈRE arrivée au dépôt
• Les sorties avant l’heure minimale sont ignorées
• L’heure de fin est une HEURE MINIMALE
• La PREMIÈRE sortie APRÈS cette heure clôture la journée

Exemple :
Heure de fin = 16h30
• 16h10 → ignorée
• 16h20 → ignorée
• 16h31 → fin de journée enregistrée à 16h31

Une fois la journée terminée :
• Aucun badge supplémentaire n’est pris en compte
"""

    const val HOME_TRAVEL = """
Mode départ domicile.

La journée est calculée entre le domicile et le travail.

Fonctionnement :
• Le départ du domicile démarre la journée
• Le retour au domicile termine la journée
• Le temps de trajet est automatiquement pris en compte

Recommandé pour :
• Déplacements quotidiens
• Journées sans point fixe
"""

    const val MANUAL = """
Mode manuel uniquement.

Aucune automatisation.

Fonctionnement :
• Aucun badge automatique
• Pas de détection GPS
• Toutes les entrées et sorties sont manuelles

Recommandé pour :
• Tests
• Situations exceptionnelles
• Désactivation complète de l’automatisme
"""

    /* ================= PARAMÈTRES GÉNÉRAUX ================= */

    const val ENTER_DISTANCE = """
Distance de déclenchement de l’entrée.

Correspond au rayon autour du lieu de travail.

Exemple :
50 m → l’entrée est validée lorsque vous êtes à 50 m ou moins du lieu.

Conseils :
• Plus la distance est grande, plus le badge est tolérant
• Trop faible → risque de non-détection
"""

    const val EXIT_DISTANCE = """
Distance de déclenchement de la sortie.

Correspond à l’éloignement nécessaire pour quitter un lieu.

Exemple :
70 m → la sortie est validée au-delà de 70 m du lieu.

Conseils :
• Doit être supérieure à la distance d’entrée
• Évite les entrées/sorties répétées
"""

    const val ENTER_DELAY = """
Temps de présence requis avant validation de l’entrée.

Fonction :
• Évite les entrées accidentelles
• Filtre les passages rapides à proximité du lieu

Exemple :
10 secondes → l’entrée est validée après 10 s dans la zone
"""

    const val EXIT_DELAY = """
Temps hors zone requis avant validation de la sortie.

Fonction :
• Évite les sorties involontaires
• Stabilise le badge automatique

Exemple :
15 secondes → sortie validée après 15 s hors zone
"""

    /* ================= MODE HOME / TRAVEL ================= */

    const val TRAVEL_TIME = """
Temps de trajet domicile ↔ travail.

Utilisé uniquement en mode départ domicile.

Fonctionnement :
• Soustrait à l’heure de départ
• Ajouté à l’heure de retour

Exemple :
Trajet = 30 min
• Départ réel 7h30 → début compté 7h00
• Retour réel 17h30 → fin comptée 18h00
"""

    /* ================= PAUSE DÉJEUNER ================= */

    const val LUNCH = """
Gestion automatique de la pause déjeuner.

Deux modes possibles :
• À l’extérieur :
  – La pause est déduite uniquement s’il y a une sortie
• Sur place :
  – La pause est déduite même sans sortie

La pause est appliquée une seule fois par jour.
"""

    const val LUNCH_DURATION = """
Durée fixe de la pause déjeuner.

Cette durée est automatiquement soustraite du temps de travail total.

Exemple :
45 minutes → 45 min retirées du temps journalier
"""

    /* ================= MODE DÉPÔT ================= */

    const val DEPOT_HOURS = """
Heures officielles du dépôt.

Heure de début :
• Toute arrivée avant est ajustée à cette heure

Heure de fin :
• Correspond à une heure MINIMALE
• Toute sortie avant est ignorée
• La première sortie après clôture la journée
"""

    const val DEPOT_ADJUST = """
Ajustement journalier appliqué après calcul.

Permet de corriger le temps final.

Exemples :
-15 → départ anticipé de 15 minutes
+10 → dépassement accepté de 10 minutes

Utile pour :
• Tolérances
• Règles internes
• Ajustements contractuels
"""
}
