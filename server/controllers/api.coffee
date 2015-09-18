fs = require('fs')
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
    fullFilePath = __dirname + '/' + fileName + '.JPEG'

    req.pipe(fs.createWriteStream(fullFilePath)).on 'finish', ->
        ftp.put fullFilePath, './data/images/' + fileName + '.JPEG', (err) ->
            fs.unlink(fullFilePath)
            if err
                console.log 'Error Saving File To FTP!'
                res.sendStatus(404)
                return
            console.log 'File Transferred Successfully!'
            picture = new Picture
            picture.fileName = fileName
            picture.save((err, picture) ->
                if err
                    console.log 'Error Saving Image Name To Database!'
                    res.sendStatus(404)
                    return
                console.log 'File Name Saved Successfully!'
            ).then ->
                res.sendStatus(200)


api.get '/api', (req, res) ->
    currentLastFile = req.query.currentLastFile
    currentLastFile = -1 if not currentLastFile
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        if err
            console.log 'Error Getting File Names From Database!'
            res.sendStatus(404)
            return
        console.log 'File Name Retrieved Successfully!'
        (
            if +picture.fileName > +currentLastFile
                res.redirect('http://rcylai.ca/waterfall/data/images/' + picture.fileName + '.JPEG')
                return
        ) for picture in pictures
        console.log 'Did Not Find File File!'
        res.sendStatus(404)

api.delete '/api/all', (req, res) ->
    Picture.find({}).sort('fileName').exec (err, pictures) ->
        if err
            console.log 'Error Getting File Names From Database!'
            res.sendStatus(404)
            return
        (
            ftp.delete './data/images/' + picture.fileName + '.JPEG', (err) ->
                if err
                    console.log 'Error Deleting File From FTP!'
                    res.sendStatus(404)
                    return
        ) for picture in pictures
        Picture.remove {}, (err) ->
            if err
                console.log 'Error Deleting File Name From Database!'
                res.sendStatus(404)
                return
            console.log 'File Names Deleted Successfully!'
        .then ->
            res.sendStatus(200)
        return

api.delete '/api', (req, res) ->
    fileName = req.query.fileName
    if not fileName
        console.log "No fileName Provided"
        res.sendStatus(404)
        return
    ftp.delete './data/images/' + fileName + '.JPEG', (err) ->
        if err
            console.log 'Error Deleting File From FTP!'
            res.sendStatus(404)
            return
        console.log 'File Deleted Successfully!'
        Picture.remove {fileName: fileName}, (err) ->
            if err
                console.log 'Error Deleting File Name From Database!'
                res.sendStatus(404)
                return
        console.log 'File Name Deleted Successfully!'
        res.sendStatus(200)


module.exports = api
