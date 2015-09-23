// Generated by CoffeeScript 1.9.3
(function() {
  var picturesApp;

  picturesApp = angular.module('picturesApp', []);

  picturesApp.controller('PicturesController', function($scope, $http, $interval) {
    var getPicture;
    $scope.pictures = [];
    getPicture = function() {
      return $http.get('/api/list').then(function(stuff) {
        if (stuff.data !== $scope.pictures) {
          return $scope.pictures = stuff.data;
        }
      });
    };
    return $interval(getPicture, 1000);
  });

}).call(this);
