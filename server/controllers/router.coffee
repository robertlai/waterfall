express = require('express')
api = require('./api')
router = express.Router()


router.use(api)

router.get '/pictures', (req, res) ->
    res.render 'pictures', title: "Pictures"

router.get '/', (req, res) ->
    res.render 'index', title: 'Waterfall API'


module.exports = router
