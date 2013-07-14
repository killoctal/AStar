package com.killoctal.pathfinding;

//import java.util.ArrayList;
import java.util.Collection;
//import org.andengine.util.adt.pool.GenericPool;
//import org.andengine.util.debug.Debug;


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
	/*final private GenericPool<Node<T>> POOL;
	
	private ArrayList<Node<T>> instancied = new ArrayList<Node<T>>();
	
	
	public NodeFactory(int pInitialNodesTotal, int pGrowth)
	{
		POOL = new GenericPool<Node<T>>(pInitialNodesTotal, pGrowth) {
			@Override
			protected Node<T> onAllocatePoolItem()
			{
				Node<T> tmp = newNode();
				return tmp;
			}
			@Override
			protected void onHandleRecycleItem(Node<T> pItem)
			{
				pItem.mParent = null;
				pItem.mIndex = null;
			}
			protected void onHandleObtainItem(Node<T> pItem)
			{
				synchronized(instancied)
				{
					instancied.add(pItem);
				}
			}
		};
	}
	
	public void recycle(Node<T> pNode)
	{
		if (pNode != null)
		{
			synchronized(instancied)
			{
				if (instancied.contains(pNode))
				{
					POOL.recyclePoolItem(pNode);
				}
				instancied.remove(pNode);
			}
		}
	}
	
	public void recycleAll()
	{
		synchronized(instancied)
		{
			for(Node<T> i : instancied)
			{
				POOL.recyclePoolItem(i);
			}
			Debug.d("Pathfinding : " + instancied.size() + " nodes recycled !");
			instancied.clear();
		}
	}
	*/
	
	/**
	 * Instancie avec un nouveau noeud, avec les valeurs remplies. Les distances
	 * cont calculées ici à l'aide des méthodes abstraites de la classe
	 *
	 * @param parent le noeud parent
	 * @param index l'index du noeud à créer
	 * @param pFinish la destination
	 * @return le nouveau noeud
	 */
	final protected Node<T> prepareNode(final Node<T> parent, final T index, final T pFinish)
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
		
		Node<T> tmpNode = newNode();//POOL.obtainPoolItem();
		tmpNode.set(parent, index, g, d);
		
		return tmpNode;
	}
	
	
	
	/**
	 * Crée un nouveau node
	 */
	protected Node<T> newNode()
	{
		return new Node<T>();
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

