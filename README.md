# shortest-path

Try graph queries in 5 min.

![](./graph.png)

- [Using Graph Server](#Using-Graph-Server) with Database
- [Using Graph Studio](#Using-Graph-Studio) of Autonomous Database

## Using Graph Server

### Setup

Install JDK 11 on your Linux.

Download [Oracle Graph Server](https://www.oracle.com/database/technologies/spatialandgraph/property-graph-features/graph-server-and-client/graph-server-and-client-downloads.html).

Install Oracle Graph Server.

### Try graph queries

Locate graph dataset files.

    /<path_to_the_files>/route.csv
    /<path_to_the_files>/route.json

Open JShell.

    cd /opt/oracle/graph/bin
    ./opg-jshell

    var graph = session.readGraphWithProperties("/<path_to_the_files>/route.json");

Run a shortest path query.

[`shortest-path.pgql`](./pgql/shortest-path.pgql)

    graph.queryPgql(
      " SELECT a.name AS a, b.name AS b, COUNT(e) AS path_length, SUM(e.cost) AS total_cost, ARRAY_AGG(n.name) AS nodes "
    + " FROM MATCH TOP 3 SHORTEST ( (a) (-[e]->(n))* (b) ) "
    + " WHERE a.name = 'A' AND b.name = 'H' "
    ).print();

    +-------------------------------------------------+
    | a | b | path_length | total_cost | nodes        |
    +-------------------------------------------------+
    | A | H | 3           | 12.0       | [B, E, H]    |
    | A | H | 3           | 11.0       | [D, G, H]    |
    | A | H | 4           | 15.0       | [B, C, E, H] |
    +-------------------------------------------------+

Run a cheapest path query.

[`cheapest-path.pgql`](./pgql/cheapest-path.pgql)

    graph.queryPgql(
      " SELECT a.name AS a, b.name AS b, COUNT(e) AS path_length, SUM(e.cost) AS total_cost, ARRAY_AGG(n.name) AS nodes "
    + " FROM MATCH TOP 3 CHEAPEST ( (a) (-[e]->(n) COST e.cost)* (b) ) "
    + " WHERE a.name = 'A' AND b.name = 'H' "
    ).print();

    +----------------------------------------------------+
    | a | b | path_length | total_cost | nodes           |
    +----------------------------------------------------+
    | A | H | 5           | 10.0       | [B, E, D, G, H] |
    | A | H | 3           | 11.0       | [D, G, H]       |
    | A | H | 3           | 12.0       | [B, E, H]       |
    +----------------------------------------------------+

Exit JShell.

    /exit

### Using Graph Viz

Graph Viz can also return the result as a table.

    SELECT a.name AS a, b.name AS b, COUNT(e) AS path_length, SUM(e.cost) AS total_cost, ARRAY_AGG(n.name) AS nodes, ARRAY_AGG(ID(e)) AS edges
    FROM MATCH CHEAPEST ( (a) (-[e]->(n) COST e.cost)* (b) )
    WHERE a.name = 'A' AND b.name = 'H'

![](./img/screen01.jpg)

However, the path detected is visualized as a dot-line only.

    SELECT a, b
    FROM MATCH CHEAPEST ( (a) (-[e]->(n) COST e.cost)* (b) )
    WHERE a.name = 'A' AND b.name = 'H'

![](./img/screen02.jpg)

To show the intermediate nodes, visualize the list of edges.

    SELECT *
    FROM MATCH ()-[e]->()
    WHERE ID(e) IN (0, 3, 8, 7, 13)

![](./img/screen03.jpg)

## Using Graph Studio
Using Graph Studio
Load data into ADW with route.csv and routesource.csv dataset files.

route.csv

```
SNAME,DNAME,COST
A,B,1
A,D,6
B,C,5
B,E,3
C,E,1
C,F,7
D,B,2
D,G,3
E,D,1
E,H,8
F,E,3
F,H,13
G,E,2
G,H,2
```
routesource.csv

```
NAME
A
B
C
D
E
F
G
H
```
Create Model with dataset files.
![image](https://user-images.githubusercontent.com/82792908/115204324-f1a8c500-a12a-11eb-94b2-37db4540ac87.png)

Generated PGQL statement.

```
CREATE PROPERTY GRAPH draft_1616572432113
  VERTEX TABLES (
    test01.routesource
      KEY ( name )
      PROPERTIES ( name )
  )
  EDGE TABLES (
    test01.route
      SOURCE KEY ( sname ) REFERENCES routesource
      DESTINATION KEY ( dname ) REFERENCES routesource
      PROPERTIES ( cost, dname, sname )
  )
```
Load graph into an in-memory representation.
![image](https://user-images.githubusercontent.com/82792908/115204380-01280e00-a12b-11eb-8854-4349f7c79555.png)
![image](https://user-images.githubusercontent.com/82792908/115204414-0ab17600-a12b-11eb-91c2-06bf05143a6c.png)

The job of "Load graph into an in-memory" is proceeded successfully.
![image](https://user-images.githubusercontent.com/82792908/115204444-12711a80-a12b-11eb-896e-1bf30232d885.png)


Run a shortest path query, Graph Studio return the result as a table.

```
%pgql-pgx
SELECT a.name AS a, b.name AS b, COUNT(e) AS path_length, SUM(e.cost) AS total_cost, ARRAY_AGG(n.name) AS nodes
FROM MATCH TOP 3 SHORTEST ((a)(-[e]->(n))*(b)) ON ROUTE01
WHERE a.name='A' AND b.name='H'
```
![image](https://user-images.githubusercontent.com/82792908/115204490-20bf3680-a12b-11eb-8e50-4fb0ad946d62.png)


Run a cheapest path query. Graph Studio return the result as a table.

```
%pgql-pgx
SELECT a.name AS a, b.name AS b, COUNT(e) AS path_length, SUM(e.cost) AS total_cost, ARRAY_AGG(n.name) AS nodes, ARRAY_AGG(ID(e)) AS edges 
FROM MATCH top 5 CHEAPEST ((a) (-[e]->(n) COST e.cost)*(b)) ON ROUTE01 
WHERE a.name='A' AND b.name='H'
```
![image](https://user-images.githubusercontent.com/82792908/115204547-2ddc2580-a12b-11eb-84c6-beb6820efe0b.png)

