
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import ReservationManager from "./components/ReservationManager"

import TicketManager from "./components/TicketManager"

import PriceManager from "./components/PriceManager"


import View from "./components/View"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/tickets',
                name: 'TicketManager',
                component: TicketManager
            },

            {
                path: '/prices',
                name: 'PriceManager',
                component: PriceManager
            },


            {
                path: '/views',
                name: 'View',
                component: View
            },


    ]
})
