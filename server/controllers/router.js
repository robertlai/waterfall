// Generated by CoffeeScript 1.9.3
(function() {
  var api, express, router;

  express = require('express');

  api = require('./api');

  router = express.Router();

  router.use(api);

  router.get('/', function(req, res, next) {
    return res.render('index', {
      title: 'Waterfall API'
    });
  });

  module.exports = router;

}).call(this);
