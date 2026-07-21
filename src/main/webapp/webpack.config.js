const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')

module.exports = {
  entry: './WEB-INF/app/index.js',
  output: {
    path: path.resolve(__dirname, 'static/dist'),
    // The content hash lets the bundle be cached indefinitely; index.html always
    // points at the current one. See WebApplicationConfig.addResourceHandlers.
    filename: 'index_bundle.[contenthash].js',
    clean: true,
    publicPath: process.env.NODE_ENV === 'production' ? '/static/dist/' : '/'
  },
  resolve: {
    alias: {
      app: path.resolve(__dirname, 'WEB-INF/app')
    }
  },
  module: {
    rules: [
      // load css files, including tailwindcss
      {
        test: /\.css$/i,
        use: ["style-loader", "css-loader", "postcss-loader"],
      },
      // Bundle images referenced from css (small ones are inlined as data URIs)
      {
        test: /\.png$/i,
        type: 'asset'
      },
      // Transpile js
      {
        test: /\.(js)$/,
        exclude: /node_modules/,
        use: 'babel-loader'
      },
    ]
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: 'WEB-INF/app/index.html'
    })
  ],
  mode: process.env.NODE_ENV === 'production' ? 'production' : 'development',
  devServer: {
    port: 3000,
    // Send api and legacy page requests for these paths to the Tomcat instance while in dev mode.
    proxy: [
      {
        context: ['/api', '/admin', '/job', '/globals', '/static', '/docs', '/admindocs', '/css', '/favicon.ico'],
        target: 'http://localhost:8080',
        secure: false,
        changeOrigin: true,
      }
    ],
    historyApiFallback: {
      disableDotRule: true,
    },
    static: {
      directory: path.resolve(__dirname, 'static')
    }
  },
  devtool: process.env.NODE_ENV === 'production' ? false : 'eval-source-map'
}
