package com.killoctal.pathfinding;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * Algorithme de calcul du chemin le plus court via l'algorithme A*
 *
 * L'implémentation est basée en partie sur : 
 * {@link http://code.google.com/p/a-star/source/browse/trunk/java/AStar.java?r=8}
 * 
 * Ressource trouvée à http://finandsys.com/finandsys-blog/index.php?post/2011/09/07/Une-impl%C3%A9mentation-en-Java-de-l-algorithme-A*-(A-star)
 *
 * @author Moloch
 * @author Gabriel Schlozer
 * @since 1.0
 * @version 2.0
 * @param <T> le type d'index associé à l'algorithme
 * 
 * @code
 * Astar<Facet> myAstar = new Astar<Facet>( ... );
 * if (myAstar.compute(fromHere, toHere))
 * {
 *     print("You have all these ways : ");
 *     
 *     List<Facet> iWay = myAstar.nextWay();
 *     do
 *     {
 *         print(iWay);
 *         
 *         iWay = myAstar.nextWay();
 *     }
 *     while(iWay != null);
 * }
 * else
 * {
 *     print("There is no ways. Want to come closer ?");
 * }
 * @endcode
 */
public class AStar<T>
{
	private NodeFactory<T> mNodeFactory;
	
	final private Queue<Node<T>> mOpenSet;
	final private HashMap<T, Node<T>> mClosedSet;
	
	final private ArrayList<Node<T>> mWays;
	
	
	/**
	 * Instancie un nouveau calculateur de chemin par algorithme A*
	 *
	 * @param successorComputer Le fournisseur de l'algorithme calculant les successeurs
	 * @param pNodeFactory le créateur du noeud
	 */
	public AStar(final NodeFactory<T> pNodeFactory)
	{
		setNodeFactory(pNodeFactory);
		
		mOpenSet = new PriorityQueue<Node<T>>();
		mClosedSet = new HashMap<T, Node<T>>();
		mWays = new ArrayList<Node<T>>();
	}
	
	
	public void setNodeFactory(NodeFactory<T> pNodeFactory)
	{
		if (pNodeFactory == null)
		{
			throw new IllegalArgumentException("NodeFactory can't be null");
		}
		
		mNodeFactory = pNodeFactory;
	}
	
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
	 * Remplit les paths possibles autour du noeud spécifié
	 *
	 * @param pCurrent le noeud autour duquel on teste les voies
	 * @param pGoal la destination finale
	 */
	private boolean expand(final Node<T> pCurrent, final T pGoal)
	{
		final T currentIndex = pCurrent.getIndex();
		final Node<T> tmpLastComputedNode = mClosedSet.get(currentIndex);
		
		if (tmpLastComputedNode == null || pCurrent.getF() < tmpLastComputedNode.getF())
		{
			mClosedSet.put(currentIndex, pCurrent);
			
			for (T iNeighbor : mNodeFactory.findNeighbors(pCurrent))
			{
				if (iNeighbor != pCurrent.getParent())
				{
					Node<T> newNode = mNodeFactory.instanciateNode(pCurrent, iNeighbor, pGoal);
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
	 * Renvoie la liste des noeuds représentant le parcours optimisé par A*
	 *
	 * @param pStart le point de départ
	 * @param pGoal le point d'arrivée espéré
	 * @param pMaxWays Nombre maximum de chemins à trouver
	 * 
	 * @return TRUE if way reach the target (returned by nextWay() ),
	 *         FALSE otherwise (you can compute the closest way using computeToClosest() )
	 */
	final public boolean compute(final T pStart, final T pGoal, int pMaxWays)
	{
        // Reset lists
        mOpenSet.clear();
		
		mClosedSet.clear();
		mWays.clear();
		
		pMaxWays = Math.max(1, pMaxWays);
		
		
		// First node is the root
		Node<T> iNode = mNodeFactory.instanciateNode(null, pStart, pGoal);
		
        // While opensets exists
        while ( expand(iNode, pGoal) )
        {
        	// Takes the current node
            iNode = mOpenSet.poll();
            
            // If the current index is the goal index, the caomputering is finished
            if (iNode.getIndex().equals(pGoal))
            {
            	mWays.add(iNode);
            	
            	if ( --pMaxWays == 0)
            	{
	                break;
            	}
            }
        }
        
        // On enlève déjà
        mClosedSet.remove(pGoal);
        
        return ! mWays.isEmpty();
    }
	
	
	
	/**
	 * @overload
	 */
	final public boolean compute(final T pStart, final T pGoal)
	{
		return compute(pStart, pGoal, 1);
	}
	
	
	
	
	/**
	 * @brief Calculer les chemins les plus proches
	 * @return TRUE si des chemins existent
	 * 
	 * @warning Efface les chemins qui atteignent la cible
	 */
	final public boolean computeToClosest()
	{
		// On efface les anciens chemins
		mWays.clear();
		
		if (mClosedSet.isEmpty())
		{
			return false;
		}
		
		// On remplace les nodes qui attreignent la cible par les nodes calculés
		mWays.addAll( mClosedSet.values() );
		mClosedSet.clear();
		
		// On trie les nodes du plus proche de la cible au plus éloigné
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
	
	
	
	final public ArrayList<T> nextWay()
	{
		if (mWays.isEmpty())
		{
			return null;
		}
		
		ArrayList<T> tmpFinalWay = new ArrayList<T>();
    	for(Node<T> i = mWays.remove(0) ; i != null ; i = i.getParent())
        {
        	tmpFinalWay.add(0, i.getIndex() );
        }
    	
    	return tmpFinalWay;
	}
	
}

