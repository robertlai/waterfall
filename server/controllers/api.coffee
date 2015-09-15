express = require('express')
api = express.Router()


api.get '/api/test', (req, res) ->
    res.json "testing"
