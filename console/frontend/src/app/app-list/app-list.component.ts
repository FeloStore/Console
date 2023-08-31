// Copyright 2023 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

import { NgFor } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatGridListModule } from '@angular/material/grid-list';

import { App } from '../app';
import { AppCardComponent } from '../app-card/app-card.component';
import { AppService } from '../app.service';

@Component({
    selector: 'app-app-list',
    standalone: true,
    imports: [AppCardComponent, MatGridListModule, NgFor],
    templateUrl: './app-list.component.html',
    styleUrls: ['./app-list.component.scss']
})
export class AppListComponent implements OnInit {
    apps: App[] = [];

    constructor(private appService: AppService) {}

    ngOnInit(): void {
        this.appService.getApps().subscribe(apps => this.apps = apps);
    }
}
