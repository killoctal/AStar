package com.killoctal.pathfinding;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
	

/**
 * @brief A* (AStar) algorithm implementation
 * 
 * Find more sources on https://github.com/killoctal and http://killoctal.com
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
 *     while ( (List<Facet> iWay = myAstar.nextWay() ) != null )
 *     {
 *         print(iWay);
 *     }
 * }
 * else
 * {
 *     print("There is no ways. Want to come closer ?");
 *     if (myAstar.toClosest())
 *     {
 *         print("You have all these ways : ");
 *         while ( (List<Facet> iWay = myAstar.nextWay() ) != null )
 *         {
 *             print(iWay);
 *         }
 *     }
 * }
 * @endcode
 * 
 * @author Moloch
 * @author Gabriel Schlozer contact@killoctal.com
 * @copyright GNU Lesser General Public License LGPLv3 http://www.gnu.org/licenses/lgpl.html
 */
public class AStar<T>
{
	/// The node factory
	private NodeFactory<T> mNodeFactory;
	
	final private PriorityQueue<Node<T>> mOpenSet;
	final private HashMap<T, Node<T>> mClosedSet;
	final private ArrayList<Node<T>> mWays;
	private double mMaxCost;
	
	
	
	/**
	 * @brief Constructor
	 * @param pNodeFactory The node factory to use (can be changed later with setNodeFactory() )
	 */
	public AStar(NodeFactory<T> pNodeFactory)
	{
		setNodeFactory(pNodeFactory);
		
		mOpenSet = new PriorityQueue<Node<T>>();
		mClosedSet = new HashMap<T, Node<T>>();
		mWays = new ArrayList<Node<T>>();
		mMaxCost = Double.MAX_VALUE;
	}
	
	
	
	/**
	 * @overload Maximum 1 way will be computed
	 */
	final public boolean compute(final T pStart, final T pGoal)
	{
		return compute(pStart, pGoal, null, 1, Double.MAX_VALUE);
	}
	
	
	
	/**
	 * @brief Compute a new way to target
	 *
	 * @param pStart Starting point
	 * @param pFinish Finish point
	 * @param pFinishSurface The finish surface (set NULL if no surface)
	 * @param pMaxWays Maximum ways to find (minimum 1)
	 * @param pMaxCost Max allowed cost of the way 
	 * 
	 * @return TRUE if way reach the target (returned by nextWay() ),
	 *		 FALSE otherwise (you can compute the closest way using computeToClosest() )
	 */
	final public boolean compute(T pStart, T pFinish, Set<T> pFinishSurface, int pMaxWays, double pMaxCost)
	{
		//mNodeFactory.recycleAll();
		
		mOpenSet.clear();
		mClosedSet.clear();		
		mWays.clear();
		mMaxCost = pMaxCost;
		
		// Minimum 1 way
		pMaxWays = Math.max(1, pMaxWays);
		
		// Basic test
		if (pFinish.equals(pStart) || (pFinishSurface != null && pFinishSurface.contains(pStart)))
		{
			return true;
		}
		
		// First node is the root
		Node<T> iNode = mNodeFactory.prepareNode(null, pStart, pFinish);
		
		// While opensets exists
		while ( expand(iNode, pFinish) )
		{
			// Takes the current node
			iNode = mOpenSet.poll();
			
			// If the current index is the goal index or is in the surface, the computering is finished
			if (pFinish.equals( iNode.getIndex() ) || (pFinishSurface != null && pFinishSurface.contains( iNode.getIndex() )))
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
		
		if (mWays.isEmpty())
		{
			return false;
		}
		
		return true;
	}
	
	
	
	/**
	 * @brief Get the neighboors of the node
	 *  
	 * @param pCurrent The current node around which one we are checking ways
	 * @param pFinish The finish point
	 * 
	 * @warning The node itself must not be his own neighbor !
	 */
	private boolean expand(final Node<T> pCurrent, final T pFinish)
	{
		T tmpCurrentIndex = pCurrent.getIndex();
		Node<T> tmpLastComputedNode = mClosedSet.get(tmpCurrentIndex);
		
		if (tmpLastComputedNode == null || (pCurrent.getF() < tmpLastComputedNode.getF() && pCurrent.getF() <= mMaxCost))
		{
			// Replaces the old node
			mClosedSet.put(tmpCurrentIndex, pCurrent);
			
			// Recycles the old node
			//mNodeFactory.recycle(tmpLastComputedNode);
			
			// Using thread-safe synchronize block
			Collection<T> tmpNeighbors = mNodeFactory.findNeighbors(pCurrent);
			synchronized(tmpNeighbors)
			{
				Iterator<T> tmpIterator = tmpNeighbors.iterator();
				while(tmpIterator.hasNext())
				{
					T iNeighbor = tmpIterator.next();
					if (iNeighbor != pCurrent.parentNode())
					{
						Node<T> newNode = mNodeFactory.prepareNode(pCurrent, iNeighbor, pFinish);
						mOpenSet.add(newNode);
					}
				}
			}
		}
		
		return ! mOpenSet.isEmpty();
	}
	
	
	
	/**
	 * @brief Get the next full way
	 * @return A way or empty list if no next way
	 * 
	 * @note For more data about the ways you can get the ways by method getWaysNodes()
	 */
	final public List<T> nextWay()
	{
		// If no more ways
		if (mWays.isEmpty())
		{
			return Collections.<T>emptyList();
		}
		
		ArrayList<T> tmpFinalWay = new ArrayList<T>();
		// Computes the final way by get back to the parent
		for(Node<T> i = mWays.remove(0) ; i != null ; i = i.parentNode())
		{
			tmpFinalWay.add(0, i.getIndex() );
		}
		
		/*// If start index is the same as end index, clear the path
		if (! tmpFinalWay.isEmpty())
		{
			if (tmpFinalWay.get(0).equals(  tmpFinalWay.get(tmpFinalWay.size()-1) ))
			{
				tmpFinalWay.clear();
			}
		}*/
		
		return tmpFinalWay;
	}
	
	
	
	/**
	 * @brief Compute the way that approach with a minimum distance to the target if it is unreachable 
	 * 
	 * If compute() returned false, use this method to find the way approaching closest to the target
	 * 
	 * @return TRUE if the way exists
	 * @warning Clear all previously computed ways 
	 * @see toShortest() Same idea 
	 */
	final public boolean toClosest()
	{
		if (! prepareToNearest())
		{
			return false;
		}
		
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
	 * @brief Compute the shortest way approaching the target
	 * 
	 * If compute() returned false, use this method to find the way approaching closest to the target
	 * 
	 * @return TRUE if the way exists
	 * @warning Clear all previously computed ways 
	 * @see toClosest() Same idea
	 */
	final public boolean toShortest()
	{
		if (! prepareToNearest())
		{
			return false;
		}
		
		// Sorting nodes by closest to awayest
		Collections.<Node<T>>sort(mWays, new Comparator<Node<T>>() {
			@Override
			public int compare(Node<T> lhs, Node<T> rhs)
			{
				if (lhs.getF() < rhs.getF())
				{
					return -1;
				}
				else if (lhs.getF() > rhs.getF())
				{
					return 1;
				}
				
				return lhs.compareTo(rhs);
			}
		});
		
		return true;
	}
	
	
	
	/**
	 * @brief Change the node factory when using the same AStar instance
	 * @param pNodeFactory The node factory to use (can not be null)
	 */
	public void setNodeFactory(NodeFactory<T> pNodeFactory)
	{
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
	 * @note The @code getWaysNodes().get(0) @endcode will be the next way returned by nextWay()
	 * @warning Be carefull with concurrent programming
	 */
	final public ArrayList<Node<T>> getWaysNodes()
	{
		return mWays;
	}
	
	
	
	private boolean prepareToNearest()
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
		
		return true;
	}
	
}

