// Generated by CoffeeScript 1.9.3
(function() {
  var express, router;

  express = require('express');

  router = express.Router();

  router.get('/', function(req, res, next) {
    return res.render('index', {
      title: 'Express'
    });
  });

  module.exports = router;

}).call(this);
