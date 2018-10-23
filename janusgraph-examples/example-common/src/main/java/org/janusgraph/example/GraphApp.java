// Copyright 2017 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.janusgraph.example;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.Bindings;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.attribute.Geoshape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphApp.class);

    protected String propFileName;
    protected Configuration conf;
    protected GraphTraversalSource g;
    protected JanusGraph graph;
    protected boolean supportsTransactions;
    protected boolean supportsSchema;
    protected boolean supportsGeoshape;

    protected static Bindings b = Bindings.instance();

    /**
     * Constructs a graph app using the given properties.
     * @param fileName location of the properties file
     */
    public GraphApp(final String fileName) {
        propFileName = fileName;
    }

    /**
     * Opens the graph instance. If the graph instance does not exist, a new
     * graph instance is initialized.
     */
    public GraphTraversalSource openGraph() throws ConfigurationException {
        LOGGER.info("opening graph");
        conf = new PropertiesConfiguration(propFileName);
        graph = JanusGraphFactory.open(conf);
        g = graph.traversal();
//---------------------------- test ---------------------------------
//        conf = new PropertiesConfiguration("E:\\ideaCode\\janusGraph\\janusgraph-examples\\example-common\\conf\\jgex-inmemory.properties");
//        graph = GraphFactory.open(conf);
//        try {
//            g = graph.traversal().withRemote("E:\\ideaCode\\janusGraph\\janusgraph-examples\\example-hbase\\conf\\jgex-remote.properties");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//--------------------------------------------------------------------
        return g;
    }

    /**
     * Closes the graph instance.
     */
    public void closeGraph() throws Exception {
        LOGGER.info("closing graph");
        try {
            if (g != null) {
                g.close();
            }
            if (graph != null) {
                graph.close();
            }
        } finally {
            g = null;
            graph = null;
        }
    }

    /**
     * 定义在了父类之中
     */
    /**
     * Drops the graph instance. The default implementation does nothing.
     */
    public void dropGraph() throws Exception {
    }

    /**
     * Creates the graph schema. The default implementation does nothing.
     */
    public void createSchema() {
    }

    /**
     * 设置supportsTransactions 标志位来判断是否支持事务！
     */
    /**
     * Adds the vertices, edges, and properties to the graph.
     */
    public void createElements() {
        try {
            // naive check if the graph was previously created 检查先前是不是已经创建了
            if (g.V().has("name", "saturn").hasNext()) {
                if (supportsTransactions) { //这个设置是否支持事务！
                    g.tx().rollback();
                }
                return;
            }
            LOGGER.info("creating elements");

            // see GraphOfTheGodsFactory.java

            final Vertex saturn = g.addV("titan").property("name", "saturn").property("age", 10000).next();
            final Vertex sky = g.addV("location").property("name", "sky").next();
            final Vertex sea = g.addV("location").property("name", "sea").next();
            final Vertex jupiter = g.addV("god").property("name", "jupiter").property("age", 5000).next();
            final Vertex neptune = g.addV("god").property("name", "neptune").property("age", 4500).next();
            final Vertex hercules = g.addV("demigod").property("name", "hercules").property("age", 30).next();
            final Vertex alcmene = g.addV("human").property("name", "alcmene").property("age", 45).next();
            final Vertex pluto = g.addV("god").property("name", "pluto").property("age", 4000).next();
            final Vertex nemean = g.addV("monster").property("name", "nemean").next();
            final Vertex hydra = g.addV("monster").property("name", "hydra").next();
            final Vertex cerberus = g.addV("monster").property("name", "cerberus").next();
            final Vertex tartarus = g.addV("location").property("name", "tartarus").next();

            g.V(jupiter).as("a").V(saturn).addE("father").from("a").next();
            g.V(jupiter).as("a").V(sky).addE("lives").property("reason", "loves fresh breezes").from("a").next();
            g.V(jupiter).as("a").V(neptune).addE("brother").from("a").next();
            g.V(jupiter).as("a").V(pluto).addE("brother").from("a").next();

            g.V(neptune).as("a").V(sea).addE("lives").property("reason", "loves waves").from("a").next();
            g.V(neptune).as("a").V(jupiter).addE("brother").from("a").next();
            g.V(neptune).as("a").V(pluto).addE("brother").from("a").next();

            g.V(hercules).as("a").V(jupiter).addE("father").from("a").next();
            g.V(hercules).as("a").V(alcmene).addE("mother").from("a").next();

            if (supportsGeoshape) {
                g.V(hercules).as("a").V(nemean).addE("battled").property("time", 1)
                        .property("place", Geoshape.point(38.1f, 23.7f)).from("a").next();
                g.V(hercules).as("a").V(hydra).addE("battled").property("time", 2)
                        .property("place", Geoshape.point(37.7f, 23.9f)).from("a").next();
                g.V(hercules).as("a").V(cerberus).addE("battled").property("time", 12)
                        .property("place", Geoshape.point(39f, 22f)).from("a").next();
            } else {
                g.V(hercules).as("a").V(nemean).addE("battled").property("time", 1)
                        .property("place", getGeoFloatArray(38.1f, 23.7f)).from("a").next();
                g.V(hercules).as("a").V(hydra).addE("battled").property("time", 2)
                        .property("place", getGeoFloatArray(37.7f, 23.9f)).from("a").next();
                g.V(hercules).as("a").V(cerberus).addE("battled").property("time", 12)
                        .property("place", getGeoFloatArray(39f, 22f)).from("a").next();
            }

            g.V(pluto).as("a").V(jupiter).addE("brother").from("a").next();
            g.V(pluto).as("a").V(neptune).addE("brother").from("a").next();
            g.V(pluto).as("a").V(tartarus).addE("lives").property("reason", "no fear of death").from("a").next();
            g.V(pluto).as("a").V(cerberus).addE("pet").from("a").next();

            g.V(cerberus).as("a").V(tartarus).addE("lives").from("a").next();

            if (supportsTransactions) {
                g.tx().commit();
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if (supportsTransactions) {
                g.tx().rollback();
            }
        }
    }

    /**
     * Returns the geographical coordinates as a float array.
     */
    protected float[] getGeoFloatArray(final float lat, final float lon) {
        final float[] fa = { lat, lon };
        return fa;
    }


    /**
     * 4. Run traversal queries to get data from the graph
     *      这里使用的是读操作/查询操作
     *      tryNext, valueMap
     * 5. Make updates to the graph
     * 6. Close the graph
     */
    /**
     * Runs some traversal queries to get data from the graph.
     */
    public void readElements() {
        try {
            if (g == null) {
                return;
            }

            LOGGER.info("reading elements");

            // look up vertex by name can use a composite index in JanusGraph
            final Optional<Map<Object, Object>> v = g.V().has("name", "jupiter").valueMap(true).tryNext();
            if (v.isPresent()) { //检查是否还在
                LOGGER.info(v.get().toString());
            } else {
                LOGGER.warn("jupiter not found");
            }

            // look up an incident edge
            //使用valueMap true来获取 tryNext(好像返回的都是迭代器结构)结尾
            final Optional<Map<Object, Object>> edge = g.V().has("name", "hercules").outE("battled").as("e").inV()
                    .has("name", "hydra").select("e").valueMap(true).tryNext();
            if (edge.isPresent()) {
                LOGGER.info(edge.get().toString());
            } else {
                LOGGER.warn("hercules battled hydra not found");
            }

            // numerical range query can use a mixed index in JanusGraph
            // 可以使用P来查看支持的方法！
            final List<Object> list = g.V().has("age", P.gte(5000)).values("age").toList();
            LOGGER.info(list.toString());

            // pluto might be deleted
            final boolean plutoExists = g.V().has("name", "pluto").hasNext();
            if (plutoExists) {
                LOGGER.info("pluto exists");
            } else {
                LOGGER.warn("pluto not found");
            }

            // look up jupiter's brothers
            final List<Object> brothers = g.V().has("name", "jupiter").both("brother").values("name").dedup().toList();
            LOGGER.info("jupiter's brothers: " + brothers.toString());

        } finally {
            // the default behavior automatically starts a transaction for
            // any graph interaction, so it is best to finish the transaction
            // even for read-only graph query operations
            if (supportsTransactions) {
                g.tx().rollback();
            }
        }
    }

    /**
     * 5. Make updates to the graph
     *      直接查询寻到节点设置property就可以！
     * 6. Close the graph
     */
    /**
     * Makes an update to the existing graph structure. Does not create any
     * new vertices or edges.
     */
    public void updateElements() {
        try {
            if (g == null) {
                return;
            }
            LOGGER.info("updating elements");
            //更新了一把时间戳
            final long ts = System.currentTimeMillis();
            final Optional<Map<Object, Object>> v = g.V().has("name", "jupiter").valueMap(true).tryNext();
            g.V().has("name", "jupiter").property("ts", ts).iterate();

            //如果支持索引的话
            if (supportsTransactions) {
                g.tx().commit();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if (supportsTransactions) {
                g.tx().rollback();
            }
        }
    }

    /**
     * 查询到，然后drop
     */
    /**
     * Deletes elements from the graph structure. When a vertex is deleted,
     * its incident edges are also deleted.
     */
    public void deleteElements() {
        try {
            if (g == null) {
                return;
            }
            if (supportsTransactions) {
                g.tx().commit();
            }
            LOGGER.info("deleting elements");
            // note that this will succeed whether or not pluto exists
            g.V().has("name", "pluto").drop().iterate();
            if (supportsTransactions) {
                g.tx().commit();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if (supportsTransactions) {
                g.tx().rollback();
            }
        }
    }

    /**
     * Run the entire application:
     * 1. Open and initialize the graph
     * 2. Define the schema
     * 3. Build the graph
     * 4. Run traversal queries to get data from the graph
     * 5. Make updates to the graph
     * 6. Close the graph
     */
    public void runApp() {
        try {
            // open and initialize the graph
            openGraph();

            // define the schema before loading data
            if (supportsSchema) {
                createSchema();
            }

            // build the graph structure
            createElements();
            // read to see they were made
            readElements();

            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep((long) (Math.random() * 500) + 500);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                // update some graph elements with changes
                updateElements();
                // read to see the changes were made
                readElements();
            }

            // delete some graph elements
            deleteElements();
            // read to see the changes were made
            readElements();

            // close the graph
            closeGraph();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
