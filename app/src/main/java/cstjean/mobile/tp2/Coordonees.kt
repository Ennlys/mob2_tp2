package cstjean.mobile.tp2

/**
 *  Data classe qui permet de garder en paramètres les points de rally
 *
 * @property latitude la latitude d'une coordonees
 * @property longitude la longitude d'une coordonees
 * @property visite Vrai si la coordonees à été visité
 *
 * @author Joseph Duquet
 * @author Ennlys Granger-Corbeil
 */
data class Coordonees(val latitude: Double,
                      val longitude : Double,
                      var visite : Boolean = false)
