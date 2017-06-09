package info.juanmendez.fragmentnavigator;

import org.junit.Before;
import org.junit.Test;

import info.juanmendez.fragmentnavigator.adapters.CoreNavFragment;
import info.juanmendez.fragmentnavigator.models.NavItem;
import info.juanmendez.fragmentnavigator.models.NavNode;
import info.juanmendez.fragmentnavigator.models.NavRoot;
import info.juanmendez.fragmentnavigator.models.NavStack;
import info.juanmendez.fragmentnavigator.models.TestCoreNavFragment;
import info.juanmendez.fragmentnavigator.models.TestCoreNavFragmentManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Juan Mendez on 6/2/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */

public class BasicRootNavigatorTest {

    String tagA = "fragmentA";
    String tagB = "fragmentB";
    String tagC = "fragmentC";
    String tagD = "fragmentD";
    String tagE = "fragmentE";
    String tagF = "fragmentF";

    TestCoreNavFragmentManager fragmentManagerShadow;
    CoreNavFragment fragmentA;
    CoreNavFragment fragmentB;
    CoreNavFragment fragmentC;
    CoreNavFragment fragmentD;
    CoreNavFragment fragmentE;
    CoreNavFragment fragmentF;
    NavRoot navRoot;

    @Before
    public void before(){
        navRoot = new NavRoot();
        RootNavigator.setNavRoot( navRoot );
        fragmentA = new TestCoreNavFragment(tagA);
        fragmentB = new TestCoreNavFragment(tagB);
        fragmentC = new TestCoreNavFragment(tagC);
        fragmentD = new TestCoreNavFragment(tagD);
        fragmentE = new TestCoreNavFragment(tagE);
        fragmentF = new TestCoreNavFragment(tagF);
        fragmentManagerShadow = new TestCoreNavFragmentManager();
    }


    @Test
    public void shouldBuildFragmentNode(){

        NavItem right = new NavItem(fragmentA);
        NavItem left = new NavItem(fragmentB);
        NavStack root = NavStack.build( right, left );
        assertEquals( root.getNodes().size(), 2 );
    }

    @Test
    public void shouldFMWork(){
        fragmentManagerShadow.add( tagA, fragmentA );
        fragmentManagerShadow.add( 0, fragmentB );

        assertEquals( "yes it has tagA", fragmentManagerShadow.findFragment(tagA), fragmentA );
        assertEquals( "yes it has tagB", fragmentManagerShadow.findFragment(0), fragmentB);

        fragmentManagerShadow.remove( tagA );
        fragmentManagerShadow.remove( 0 );

        assertNull( "tagA gone", fragmentManagerShadow.findFragment(tagA) );
        assertNull( "tagB gone", fragmentManagerShadow.findFragment(0) );
    }

    @Test
    public void shouldDispatchRequest(){
        fragmentManagerShadow.add( tagA, fragmentA );
        fragmentManagerShadow.add( tagB, fragmentB );

        NavItem navItemA = NavItem.build(fragmentA);
        NavItem navItemB = NavItem.build(fragmentB);

        //lets draw the fragments
        navRoot.applyNodes(navItemA, navItemB);

        //so we are going to build a dual pane...
        navRoot.request( tagA );

        navRoot.asObservable().subscribe(navItems -> {
            assertEquals( "tag is A", navItems.get(navItems.size()-1), navItemA );
        });
    }


    @Test
    public void shouldGetParentOfNode(){
        /*NavNode parentNode = navigator.getRoot().search( tagA );
        assertEquals( "it's the same root node", parentNode, navigator.getRoot() );*/

        NavItem navItemA = NavItem.build(fragmentA);
        NavItem navItemB = NavItem.build(fragmentB);


        NavItem navItemC = NavItem.build(fragmentC);
        NavItem navItemD = NavItem.build(fragmentD);

        navRoot.clear();
        navRoot.applyNodes(navItemA, navItemB.applyNodes(navItemC, navItemD) );


        NavNode result;
        NavNode match = null;

        for( NavNode node: navRoot.getNodes() ){

            result = node.search( tagC);

            if( result != null ){
                match = result;
            }
        }

        assertNotNull( "not null", match);
    }


    @Test
    public void shouldStackGoForward(){
        navRoot.clear();

        NavItem navItemA = NavItem.build(fragmentA);
        NavItem navItemB = NavItem.build(fragmentB);

        navRoot.applyNodes( NavStack.build(navItemA, navItemB) );

        //lets go to first one!
        navRoot.request( tagA );

        //if its in a stack then only show this fragment!
        assertTrue( "navItemA is visible", navItemA.getVisible() );
        assertFalse( "navItemB is invisible", navItemB.getVisible() );

        navRoot.request( tagB );

        assertFalse( "navItemA is invisible", navItemA.getVisible() );
        assertTrue( "navItemB is visibile", navItemB.getVisible() );
    }

    @Test
    public void shouldStackGoBack(){
        navRoot.clear();

        NavItem navItemA = NavItem.build(fragmentA);
        NavItem navItemB = NavItem.build(fragmentB);
        NavItem navItemC = NavItem.build(fragmentC);

        navRoot.applyNodes( NavStack.build(navItemA, navItemB, navItemC) );

        //lets go to first one!
        navRoot.request( tagA );
        navRoot.request( tagB );
        navRoot.request( tagC );

        //if its in a stack then only show this fragment!
        assertTrue( "navItemA is visible", navItemC.getVisible() );

        Boolean wentBack = navRoot.goBack();
        assertTrue("able to go back", wentBack );
        assertTrue( "navItemB is visible", navItemB.getVisible() );
        assertFalse( "navItemC is invisible", navItemC.getVisible() );
        assertFalse( "navItemA is invisible", navItemA.getVisible() );

        wentBack = navRoot.goBack();
        assertTrue("able to go back", wentBack );
        assertFalse( "navItemB is ivisible", navItemB.getVisible() );
        assertFalse( "navItemC is invisible", navItemC.getVisible() );
        assertTrue( "navItemA is visible", navItemA.getVisible() );
        
        wentBack = navRoot.goBack();
        assertFalse("navigation ended when false", wentBack );
    }

}