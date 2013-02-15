package com.killoctal.pathfinding;


/**
 * Représente un "noeud" du parcours de l'algotithme A*. Le noeud se caractèrise
 * par : g , sa distance au noeud de départ h , sa distance au noeau d'arrivée f
 * , la somme de g+h le noeud parent du noeud courant un index unique, par
 * exemple la position sur une carte
 *
 * @author Moloch
 * @since 1.0
 * @version 1.0
 * @param <T> l'index du noeud, par exemple une position
 */
public class Node<T> implements Comparable<Node<T>>
{
	///
	final private double mG;
	
	///  le cout total du trajet jusqu'a à la destination (cout reel origine-noeud + cout theorique noeud-destination)
	final private double mF;
	
	/// Distance théorique entre ce noeud et la destination
	final private double mDistance;
	
	final private Node<T> mParent;
	final private T mIndex;
	
	/**
	 * Construit un nouveau noeud
	 *
	 * @param pParent le noeud parent de celui-ci (peut etre null)
	 * @param pIndex l'index du noeud courant
	 * @param pG le cout du trajet entre l'origine et ce noeud
	 * @param pDistance cout theorique noeud-destination
	 */
	public Node(final Node<T> pParent, final T pIndex, final double pG, final double pDistance)
	{
		mParent = pParent;
		mIndex = pIndex;
		mG = pG;
		mDistance = pDistance;
		mF = mG + mDistance;
	}
	
	
	
	/**
	 * Représente le cout réel entre ce noeud et le point de départ Le cout peut
	 * recouvrir la notion de distance, mais aussi de difficulté d'accès
	 *
	 * @return le cout pour arrivée jusqu'au noeud courant
	 */
	public double getG()
	{
		return mG;
	}
	
	
	
	/**
	 * Représente le cout total du trajet en passant par ce noeud. Ce cout total
	 * est la somme de g + h C'est a dire que le cout total est en fait la somme
	 * Du cout réel entre le point de départ et le point courant (gestion de la
	 * difficulté) et du cout théorique pour arriver jusqu'au point d'arrivé
	 * (distance)
	 *
	 * @return le cout total du trajet
	 */
	public double getF()
	{
		return mF;
	}
	
	
	public double getDistance()
	{
		return mDistance;
	}
	
	
	
	/**
	 * Renvoit le noeud parent de ce noeud, permettant ainsi de remonter
	 * jusqu'au point de départ. Si le noeud courant est le noeud de départ,
	 * doit renvoyer null.
	 *
	 * @return le noeud parent
	 */
	public Node<T> getParent()
	{
		return mParent;
	}
	
	
	
	/**
	 * Renvoie l'index du noeud courant dans l'ensemble des noeuds
	 *
	 * @return l'index du noeud courant
	 */
	public T getIndex()
	{
		return mIndex;
	}
	
	
	
	/**
	 * La comparaison entre 2 noeuds doit se faire entre valeur de F
	 *
	 * @param node le noeud avec lequelle on compare
	 * @return this.f - node.f
	 */
	@Override
	public int compareTo(final Node<T> node)
	{
		if (node == this)
		{
			return 0;
		}
		
		final int result = (int) (getF() - node.getF());
		
		if (result == 0)
		{
			return -1;
		}
		
		return result;
	}
	
	
	
	@Override
	public boolean equals(final Object obj)
    {
        if (obj instanceof Node<?>)
        {
        	final Node<?> other = (Node<?>) obj;
	        if (other.getDistance() == getDistance() && other.getG() == getG() && (mParent == null || mParent.equals(other.getParent())))
	        {
	            return true;
	        }
        }
        
        return false;
    }
	
	
	@Override
	public int hashCode()
	{
		return (int) mDistance + (int) mF + ((mParent != null) ? mParent.hashCode() : 0);
	}
}

