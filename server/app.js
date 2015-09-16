// Generated by CoffeeScript 1.9.3
(function() {
  var app, bodyParser, express, logger, path, router;

  express = require('express');

  bodyParser = require('body-parser');

  logger = require('morgan');

  path = require('path');

  router = require('./controllers/router');

  app = express();

  app.set('views', path.join(__dirname, 'views'));

  app.set('view engine', 'jade');

  app.use(express["static"]('public'));

  app.use(bodyParser.text({}));

  app.use(logger('dev'));

  app.use(router);

  if (app.get('env') === 'development') {
    app.use(function(err, req, res, next) {
      res.status(err.status || 500);
      return res.render('error', {
        message: err.message,
        error: err
      });
    });
  }

  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    return res.render('error', {
      message: err.message,
      error: {}
    });
  });

  module.exports = app;

}).call(this);
