// Generated by CoffeeScript 1.9.3
(function() {
  var FTP, Picture, api, db, express, fs, ftp, mongoose, pictureSchema;

  fs = require('fs');

  express = require('express');

  mongoose = require('mongoose');

  db = require('../Utilities/DB');

  FTP = require('ftp');

  api = express.Router();

  ftp = new FTP;

  ftp.connect({
    host: 'ftp.rcylai.ca',
    port: 21,
    user: 'waterfall@rcylai.ca',
    password: 'Waterfall0pw'
  });

  pictureSchema = new mongoose.Schema({
    fileName: String
  });

  Picture = mongoose.model('picture', pictureSchema);

  api.post('/api', function(req, res) {
    var fileName, fullFilePath;
    fileName = (new Date()).getTime();
    fullFilePath = __dirname + '/' + fileName + '.JPEG';
    return req.pipe(fs.createWriteStream(fullFilePath)).on('finish', function() {
      var fileBuffer;
      fileBuffer = fs.readFileSync(fullFilePath);
      return ftp.put(fileBuffer, './data/images/' + fileName + '.JPEG', function(err) {
        var picture;
        fs.unlinkSync(fullFilePath);
        if (err) {
          console.log('Error Saving File To FTP!');
          res.sendStatus(404);
        } else {
          console.log('File transferred successfully!');
          picture = new Picture;
          picture.fileName = fileName;
          return picture.save(function(err, picture) {
            if (err) {
              console.log('Error Saving Image Name To Database!');
              res.sendStatus(404);
              return;
            }
            return console.log('');
          }).then(function() {
            return res.sendStatus(200);
          });
        }
      });
    });
  });

  api.get('/api', function(req, res) {
    var currentLastFile;
    currentLastFile = req.query.currentLastFile;
    if (!currentLastFile) {
      currentLastFile = -1;
    }
    return Picture.find({}).sort('fileName').exec(function(err, pictures) {
      var i, len, picture;
      if (err) {
        console.log('Error Getting File Name From Database!');
        res.sendStatus(404);
        return;
      }
      for (i = 0, len = pictures.length; i < len; i++) {
        picture = pictures[i];
        if (+picture.fileName > +currentLastFile) {
          res.redirect('http://rcylai.ca/waterfall/data/images/' + picture.fileName + '.JPEG');
          return;
        }
      }
      return res.sendStatus(404);
    });
  });

  module.exports = api;

}).call(this);
