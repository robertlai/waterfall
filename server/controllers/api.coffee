express = require('express')
mongoose = require('mongoose')
db = require('../Utilities/DB')
FTP = require('ftp')


api = express.Router()


ftp = new FTP
ftp.connect({
  host: 'ftp.rcylai.ca'
  port: 21
  user: 'waterfall@rcylai.ca'
  password: 'Waterfall0pw'
})


pictureSchema = new mongoose.Schema({
    fileName: String
})
Picture = mongoose.model('picture', pictureSchema)


api.post '/api', (req, res) ->
    fileName = (new Date()).getTime()

    # todo: put this path in a methods (send it the file name)
    ftp.put req, './data/images/' + fileName + '.JPEG', (err) ->
        if err
            console.log err
            res.sendStatus(500)
        else
            picture = new Picture
            picture.fileName = fileName
            picture.save (err, picture) ->
                if err
                    console.log err
                    res.sendStatus(500)
                else
                    res.sendStatus(201)


api.get '/api', (req, res) ->
    currentLastFile = req.query.currentLastFile
    currentLastFile = -1 if not currentLastFile
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        if err
            console.log err
            res.sendStatus(500)
        else
            fileToGet = null
            (
                if +picture.fileName > +currentLastFile
                    fileToGet = picture.fileName
                    break
            ) for picture in pictures
            if fileToGet
                ftp.get './data/images/' + picture.fileName + '.JPEG', (err, pictureFromFtp) ->
                    if err
                        console.log err
                        res.sendStatus(500)
                    else
                        res.set('Content-Type': 'image/jpeg')
                        res.set('fileName': picture.fileName)
                        pictureFromFtp.pipe(res)
            else
                res.sendStatus(404)

# todo: fix this (get list of images from ftp and delete them (based on containing jpeg and bing certain number of chars long))
api.delete '/api/all', (req, res) ->
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        if err
            console.log err
            res.sendStatus(500)
        else
            (
                ftp.delete './data/images/' + picture.fileName + '.JPEG', (err) ->
                    if err
                        console.log err
                        res.sendStatus(500)
            ) for picture in pictures
            Picture.remove {}, (err) ->
                if err
                    console.log err
                    res.sendStatus(500)
                else
                    res.sendStatus(200)

api.delete '/api', (req, res) ->
    fileName = req.query.fileName
    if not fileName
        res.sendStatus(500)
        return
    ftp.delete './data/images/' + fileName + '.JPEG', (err) ->
        if err
            console.log err
            res.sendStatus(500)
        else
            Picture.remove {fileName: fileName}, (err) ->
                if err
                    console.log err
                    res.sendStatus(500)
                else
                    res.sendStatus(200)


module.exports = api
