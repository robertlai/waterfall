express = require('express')
db = require('../utilities/DB')

api = express.Router()

picturesTable = db.get('pictures')

api.post '/api', (req, res) ->
    newPicture = req.query.picture
    picturesTable.insert({ image: newPicture })
    res.json "yay"

api.get '/api', (req, res) ->
    picturesTable.find {}, (err, pictures) ->
        throw err if err
        res.json pictures


module.exports = api
