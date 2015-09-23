picturesApp = angular.module('picturesApp', [])


picturesApp.controller 'PicturesController', ($scope, $http, $interval) ->

    $scope.pictures = []

    getPicture = ->
        $http.get('/api/list').then (stuff) ->
            $scope.pictures = stuff.data if stuff.data != $scope.pictures

    $interval(getPicture, 1000)