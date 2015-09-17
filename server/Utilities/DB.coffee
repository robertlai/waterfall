mongoose = require('mongoose')

db = mongoose.connect('mongodb://dev:password@ds059682.mongolab.com:59682/waterfall')


module.exports = db
