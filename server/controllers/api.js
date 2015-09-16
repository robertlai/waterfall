// Generated by CoffeeScript 1.9.3
(function() {
  var api, db, express, picturesTable;

  express = require('express');

  db = require('../Utilities/DB');

  api = express.Router();

  picturesTable = db.get('pictures');

  api.post('/api/test', function(req, res) {
    var body, number;
    number = req.query.number;
    body = req.body;
    picturesTable.insert({
      number: number,
      body: body
    });
    return res.json({
      number: number,
      body: body
    });
  });

  api.post('/api', function(req, res) {
    var newPicture;
    newPicture = req.query.picture;
    picturesTable.insert({
      image: newPicture
    });
    return res.json("yay");
  });

  api.get('/api', function(req, res) {
    return picturesTable.find({}, function(err, pictures) {
      if (err) {
        throw err;
      }
      return res.json(pictures);
    });
  });

  module.exports = api;

}).call(this);
