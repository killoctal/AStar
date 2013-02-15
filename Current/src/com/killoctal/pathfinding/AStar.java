package com.killoctal.pathfinding;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
	

/**
 * @brief A* (AStar) algorithm implementation
 * 
 * Partially based on http://code.google.com/p/a-star/source/browse/trunk/java/AStar.java?r=8
 * Gabriel's version based on Moloch's version http://finandsys.com/finandsys-blog/index.php?post/2011/09/07/Une-impl%C3%A9mentation-en-Java-de-l-algorithme-A*-(A-star)
 * 
 * @param <T> The class of index used in algorythm
 * 
 * @code
 * Astar<Facet> myAstar = new Astar<Facet>( ... );
 * if (myAstar.compute(fromHere, toHere))
 * {
 *     print("You have all these ways : ");
 *     List<Facet> iWay = myAstar.nextWay();
 *     do
 *     {
 *         print(iWay);
 *         iWay = myAstar.nextWay();
 *     }
 *     while(iWay != null);
 * }
 * else
 * {
 *     print("There is no ways. Want to come closer ?");
 * }
 * @endcode
 * 
 * @version 2.0
 * @author Moloch
 * @author Gabriel Schlozer
 * @copyright GNU Lesser General Public License LGPLv3 http://www.gnu.org/licenses/lgpl.html
 */
public class AStar<T>
{
	/// The node factory
	private NodeFactory<T> mNodeFactory;
	
	final private PriorityQueue<Node<T>> mOpenSet;
	final private HashMap<T, Node<T>> mClosedSet;
	final private ArrayList<Node<T>> mWays;
	
	
	
	/**
	 * Instancie un nouveau calculateur de chemin par algorithme A*
	 *
	 * @param successorComputer Le fournisseur de l'algorithme calculant les successeurs
	 * @param pNodeFactory le créateur du noeud
	 */
	public AStar(NodeFactory<T> pNodeFactory)
	{
		setNodeFactory(pNodeFactory);
		
		mOpenSet = new PriorityQueue<Node<T>>();
		mClosedSet = new HashMap<T, Node<T>>();
		mWays = new ArrayList<Node<T>>();
	}
	
	
	
	/**
	 * @brief Change the node factory when using the same AStar instance
	 * @param pNodeFactory The node factory to use (can not be null)
	 */
	public void setNodeFactory(NodeFactory<T> pNodeFactory)
	{
		if (pNodeFactory == null)
		{
			throw new IllegalArgumentException("NodeFactory can't be null");
		}
		
		mNodeFactory = pNodeFactory;
	}
	
	
	
	/**
	 * @brief Get the current used node factory
	 */
	public NodeFactory<T> getNodeFactory()
	{
		return mNodeFactory;
	}
	
	
	
	/**
	 * @brief Return the closest founded index 
	 * @return The closest index or null
	 * @note This cannot be used before the computing
	 * @note The @code getWays().get(0) @endcode ill be the next way returned returned by nextWay()
	 * 
	 */
	final public ArrayList<Node<T>> getWaysNodes()
	{
		return mWays;
	}
	
	
	
	/**
	 * @brief Get the neighboors of the node
	 *  
	 * @param pCurrent le noeud autour duquel on teste les voies
	 * @param pFinish la destination finale
	 * 
	 * @warning The node itself must not be his own neighbor !
	 */
	private boolean expand(final Node<T> pCurrent, final T pFinish)
	{
		T tmpCurrentIndex = pCurrent.getIndex();
		Node<T> tmpLastComputedNode = mClosedSet.get(tmpCurrentIndex);
		
		if (tmpLastComputedNode == null || pCurrent.getF() < tmpLastComputedNode.getF())
		{
			mClosedSet.put(tmpCurrentIndex, pCurrent);
			
			for (T iNeighbor : mNodeFactory.findNeighbors(pCurrent))
			{
				if (iNeighbor != pCurrent.getParent())
				{
					Node<T> newNode = mNodeFactory.instanciateNode(pCurrent, iNeighbor, pFinish);
					if (newNode != null)
					{
						mOpenSet.add(newNode);
					}
				}
			}
		}
		
		return ! mOpenSet.isEmpty();
	}
	
	
	
	/**
	 * @brief Compute a new way to target
	 *
	 * @param pStart Starting point
	 * @param pFinish Finish point
	 * @param pMaxWays Maximum ways to find
	 * 
	 * @return TRUE if way reach the target (returned by nextWay() ),
	 *		 FALSE otherwise (you can compute the closest way using computeToClosest() )
	 */
	final public boolean compute(T pStart, T pFinish, int pMaxWays)
	{
		// Reset lists
		mOpenSet.clear();
		mClosedSet.clear();
		mWays.clear();
		
		// Minimum 1 way
		pMaxWays = Math.max(1, pMaxWays);
		
		
		// First node is the root
		Node<T> iNode = mNodeFactory.instanciateNode(null, pStart, pFinish);
		
		// While opensets exists
		while ( expand(iNode, pFinish) )
		{
			// Takes the current node
			iNode = mOpenSet.poll();
			
			// If the current index is the goal index, the computering is finished
			if (iNode.getIndex().equals(pFinish))
			{
				mWays.add(iNode);
				
				// If maximum ways were found exit the main loop
				if ( --pMaxWays == 0)
				{
					break;
				}
			}
		}
		
		// On enlève déjà
		mClosedSet.remove(pFinish);
		
		return ! mWays.isEmpty();
	}
	
	
	
	/**
	 * @overload Only 1 way will be computed
	 */
	final public boolean compute(final T pStart, final T pGoal)
	{
		return compute(pStart, pGoal, 1);
	}
	
	
	
	/**
	 * @brief Calculer les chemins les plus proches
	 * 
	 * Call once this method after compute() if you have no result
	 * 
	 * @return TRUE si des chemins existent
	 * 
	 * @warning Efface les chemins qui atteignent la cible
	 */
	final public boolean computeToClosest()
	{
		// Clear old ways
		mWays.clear();
		
		// If no more ways
		if (mClosedSet.isEmpty())
		{
			return false;
		}
		
		// On remplace les nodes qui attreignent la cible par les nodes calculés
		mWays.addAll( mClosedSet.values() );
		mClosedSet.clear();
		
		// Sortinf nodes by closest to awayest
		Collections.<Node<T>>sort(mWays, new Comparator<Node<T>>() {
			@Override
			public int compare(Node<T> lhs, Node<T> rhs)
			{
				if (lhs.getDistance() < rhs.getDistance())
				{
					return -1;
				}
				else if (lhs.getDistance() > rhs.getDistance())
				{
					return 1;
				}
				
				return lhs.compareTo(rhs);
			}
		});
		
		return true;
	}
	
	
	
	/**
	 * @brief Get the next full way
	 * @return A way or NULL if no next way
	 * 
	 * @note For more data about the ways you can get the ways by method getWaysNodes()
	 */
	final public ArrayList<T> nextWay()
	{
		// If no more ways
		if (mWays.isEmpty())
		{
			return null;
		}
		
		// Computes the final way by get back to the parent
		ArrayList<T> tmpFinalWay = new ArrayList<T>();
		for(Node<T> i = mWays.remove(0) ; i != null ; i = i.getParent())
		{
			tmpFinalWay.add(0, i.getIndex() );
		}
		
		return tmpFinalWay;
	}
	
}

