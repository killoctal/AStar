package com.killoctal.pathfinding;

import java.util.Collection;


/**
 * Factory destinée à la construction de nouveau node pour l'algorithme A*
 * 
 * @version 2.0
 * @author Moloch
 * @author Gabriel Schlozer
 * @copyright GNU Lesser General Public License LGPLv3 http://www.gnu.org/licenses/lgpl.html
 */
public abstract class NodeFactory<T>
{
	/**
	 * Instancie avec un nouveau noeud, avec les valeurs remplies. Les distances
	 * cont calculées ici à l'aide des méthodes abstraites de la classe
	 *
	 * @param parent le noeud parent
	 * @param index l'index du noeud à créer
	 * @param pFinish la destination
	 * @return le nouveau noeud
	 */
	final protected Node<T> instanciateNode(final Node<T> parent, final T index, final T pFinish)
	{
		double g;
		
		if (parent == null)
		{
			g = computeDifficulty(null, index);
		}
		else
		{
			g = parent.getG();
			if (! index.equals( parent.getIndex() ))
			{
				g += computeDifficulty(parent.getIndex(), index);
			}
		}
		
		double d = computeDistance(index, pFinish);
		
		return initNode(parent, index, g, d);
	}
	
	
	/**
	 * Crée un nouveau node avec les paramètres indiqués. C'est la
	 * résponsabilité de cette méthode de positionner les paramètres
	 * correctement
	 *
	 * @param parent le noeud parent
	 * @param index l'index du noeud
	 * @param g la valeur de la distance réél à l'origine
	 * @param pDistance la valeur de la distance théorique à la destination
	 * @return le nouveau noeud
	 */
	protected Node<T> initNode(final Node<T> parent, final T index, final double g, final double pDistance)
	{
		return new Node<T>(parent, index, g, pDistance);
	}
	
	
	
	/**
	 * Renvoit la distance réel entre les 2 index, sachant que ce sont des index
	 * consecutifs. En gros l'idée, est que si les 2 index sont identiques , la
	 * méthode renvoit 0 Si le 2eme index est accessible, on renvoit un chiffre
	 * représentant la difficulité d'accès >= 1 Si le 2emem index est
	 * inaccessible, on renvoit Double.MAX_VALUE Attention, le parentIndex peut
	 * etre null
	 *
	 * @param parentIndex l'index du parent (peut etre null, si pas de parent)
	 * @param index l'index de la destination
	 *
	 * @return renvoit la distance réel
	 */
	protected abstract double computeDifficulty(T parentIndex, T index);
	
	
	
	/**
	 * Renvoit le cout théorique (distance) entre l'index et le goal Une bonne
	 * fonction theorique doit toujours etre inférieur au reel
	 *
	 * @param index l'origine
	 * @param goal la destination
	 * @return la distance
	 */
	protected abstract double computeDistance(T index, T goal);
	
	
	
	/** 
	 * Doit renvoyer les points a gauche, a droite, en haut en bas du noeud passé en paramètre 
	 * en supprimant la position du noeud parent de ce noeud 
	 *  
	 * @param node le noeud dont on cherche les voisins 
	 * @return la liste des voisins du noeud diminué de la position du parent (pour ne pas refaire un pas en arrière)
	 */
	protected abstract Collection<T> findNeighbors(Node<T> node);
}

