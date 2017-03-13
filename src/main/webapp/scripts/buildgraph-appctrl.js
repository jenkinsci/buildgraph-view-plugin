angular.module('buildgraphapp', [])
.controller('BuildGraphAppCtrl', ['$scope', '$http', '$timeout', function BuildGraphAppCtrl ($scope, $http, $timeout) {

    var buildGraphDataModel = {nodes:[],connectors:[]};
    var buildGraphPlumb = jsPlumb.getInstance({Container:"buildgraph"});
    var nodesSize = 0;
    var isBuilding = false;
    $scope.callAtTimeout = function() {
        $http.get(ajaxPath)
            .then(function(response) {
                var data = response.data;
                /* response.data can either be parsed JSON (object) or unparsed String() object
                 * (or plain type string if angular implementation changes). */
                if (typeof data === 'string' || data instanceof String) {
                    data = JSON.parse(response.data);
                }
                var buildGraph = JSON.parse(data.buildGraph);
                if(buildGraph.isBuilding || nodesSize != buildGraph.nodesSize || isBuilding != buildGraph.isBuilding) {
                    nodesSize = buildGraph.nodesSize;
                    isBuilding = buildGraph.isBuilding;
                    $scope.buildGraphViewModel = buildGraph;
                    $timeout(function() {
                        buildGraphPlumb.reset();
                        for(i = 0; i < buildGraph.connectors.length; i++) {
                            var connectorarrow = buildGraph.connectors[i];
                            buildGraphPlumb.connect({
                                source:connectorarrow.source,
                                target:connectorarrow.target,
                                overlays: [[ "Arrow", {
                                    location: 1,
                                    id: "arrow",
                                    length: 12,
                                    width: 12
                                }]],
                                anchors: [[1, 0, 1, 0, 0, 37], [0, 0, -1, 0, 0, 37]],
                                connector: ["Flowchart", { stub: 25, gap: 0, midpoint: 0, alwaysRespectStubs: true } ],
                                endpoint:[ "Blank", {} ],
                                paintStyle:{ strokeStyle: 'grey', lineWidth: '3' }
                            });
                        }
                        buildGraphPlumb.repaintEverything();
                    },200);
                }
            }
        );

        $timeout( function(){ $scope.callAtTimeout(); }, 3000);
    }
    $scope.callAtTimeout();
}])
.directive('myBuildGraph', function() {
      return {
        restrict: 'E',
        templateUrl: function(element, attrs) {
              return attrs.jenkinsurl + "/plugin/buildgraph-view/scripts/buildgraph-nodetemplate.html";
            },
        replace: true
      };
})
.filter('rawHtml', ['$sce', function($sce){
      return function(html) {
        return $sce.trustAsHtml(html);
      };
}])
