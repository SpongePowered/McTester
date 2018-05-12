const webpack = require('webpack');

module.exports = {
  target: 'node',
    plugins: [
        new webpack.IgnorePlugin(/electron/)
    ]
};
