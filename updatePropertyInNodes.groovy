import javax.jcr.Node

// set to false to update the nodes
final def dryRun = true;

// the path to search for content
final def path = '/content/geometrixx/en'

// find content with duplicate alias and node names
def buildQuery(path, type) {
    def queryManager = session.workspace.queryManager;
    def statement = "SELECT * FROM [" + type + "] AS s WHERE ISDESCENDANTNODE([" + path + "]) and [sling:alias] <> ''"
    queryManager.createQuery(statement, 'sql');
}

final def query = buildQuery(path, 'cq:PageContent');
final def result = query.execute()

duplicateAliasFoundCount = 0;
duplicateAliasRemovedCount = 0;

// iterate through each of the nodes
result.nodes.each {
    node ->
        String nodeAlias = node.getProperty('sling:alias').getString();
        String parentNodePath = node.getParent().path;
        String parentNodeName = parentNodePath.substring(parentNodePath.lastIndexOf('/')+1)
        
        if (nodeAlias == parentNodeName) {
            println 'Duplicate alias found: ' + node.path
            duplicateAliasFoundCount++

            println 'Removing alias from node...'
            node.getProperty('sling:alias').remove();
            duplicateAliasRemovedCount++;
        }
}

println 'Number Of pages with alias found :' + result.nodes.size();
println 'Number of pages with duplicate alias:' + duplicateAliasFoundCount;

if (duplicateAliasRemovedCount > 0 && dryRun == false) {
    session.save();
    println 'Number of pages with duplicate alias removed:' + duplicateAliasRemovedCount;
} else if (dryRun) {
    println 'DRY-RUN Only: No nodes removed.'
} else {
    println 'No duplicate alias nodes found.'
}
