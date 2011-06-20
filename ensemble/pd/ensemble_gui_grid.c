/*
TODO
- criar funções genéricas
- Referenciar fontes por um nome, e não por um numero
- Colocar a direcionalidade!!!
- x,y,z em float???
- colocar proteções nos códigos de entrada de dados
- ler e escrever parametros no arquivo
*/

/* ---------------------------------------------------------------------------- */
/* BASEADO NO CÓDIGO DE:                                                        */
/* audce_gui_grid.c written by Yves Degoyon 2002                                */
/* 2-dimensional audce_gui_grid simulation                                      */
/* ( simulates spatialization and interferences )                               */
/* ---------------------------------------------------------------------------- */

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <math.h>
#include <ctype.h>
#include "m_pd.h"
#include "m_imp.h"
#include "g_canvas.h"
#include "t_tk.h"
//#include "audce_gui_grid.h"

#ifdef NT
#include <io.h>
#else
#include <unistd.h>
#endif

#define AUDCE_GUI_GRID_NONE 0
#define AUDCE_GUI_GRID_SRC 1
#define AUDCE_GUI_GRID_RCV 2

#define DEFAULT_WIDTH		200
#define DEFAULT_HEIGHT		200
#define DEFAULT_PIXELSIZE	0.1
#define DEFAULT_NUM_AGENTS	2

#define RECEIVER_WIDTH	20
#define RECEIVER_HEIGHT 19
#define SPEAKER_WIDTH	20
#define SPEAKER_HEIGHT	16

char src_img[] = {"R0lGODlhFAAQANUAAAAAAP///42NjWFhYWBgYF1dXVhYWFdXV1JSUlFRUVBQUE9PT05OTk1NTUxMTEtLS0REREJCQkFBQUBAQD8/Pz09PTw8PDg4ODc3NzY2NjU1NTMzMysrKyoqKikpKSgoKCYmJiUlJSQkJCMjIyEhISAgIB8fHxwcHBsbGxcXFxQUFBISEhEREQYGBgUFBf///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAC8ALAAAAAAUABAAAAaBwJdwSCSmNsXk6/RIepTDD8AxnAxDUCmgQVQNK0kTYLwoYl+SlyDiUHDG4wQaNbQIEQW4HsAYaoQUQiMDe3BURSdCJYSFU0kZQigEjQAKQyRCYC9PLissIi1wchkcQwZCB0lwZURnT0pjXEIQGEISBFAvAE1JILlCvEMdF79KH0VBADs="};
char rcv_img[] = {"R0lGODlhFAATAPcAAAAAAP///6WkpTo6PTw8Pj09PjY2N/Pz9Dg5PTk6Pfb3+jY4PTAyNjU3OzQ2OjM1OTg6Pjs8Pjk7Pjg6PTo8PjU2Nzw9PvHy8+zt7isrKjo5NUxGNT89Nzg3NOLf1+vq55R4Mp1/Npd6NMOeRcqkSbeWR9SuU8+qUc6pUdOuVNGsU8yoUsekULiZSreYSdOuVc6qU8qnUsmmUcilUcSiT86qVMmmUsekUb+eTraWSqaJRJqAQZl/QZV8P5V8QKOJSIp0P5B5QoVwPYRvPb2gWn1qPLmdWq6UVnNiOaqSWGtcOJqFUb+lZryjZbKaYV9TNr2mbk1ELbijcVZNNlRLNbKfcVNLNr2rfqqbd829l7apir6xka2ihc/DpszCqubk36eFNZh6M6KCN51+Nb2ZRKSFPJl8OMymTLKRRcynUMejTtSuVM2oUc+qU8unUsqmUsyoU7uaTLCRSKuNRq+QSKCEQrOUS7maUYNtO6eKTL6gWXdlO6CMXbmjb6KPYlJIMqSRZnBjRk9JO7uvk83Bpj47NNHHsJiVjt3Yzjo5Nzk4NuTh2+vp5efm5FdUUOPh36VnM6tvPqVkM6qLc76QccSfhcqum+rk4Lyllj49PTg3Nzc2Nujn5/j4+PX19fHx8ebm5t/f39vb28/Pz83NzcXFxYWFhX5+fnd3d2tra19fX1VVVVNTU09PT0pKSkBAQD4+Pj09PTw8PDg4ODc3NzY2NjQ0NDIyMjExMTAwMC8vLy0tLf///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAALwALAAAAAAUABMAAAj/AHkJHCjwUiVKljwRXMhwEqRIkjAxZKjAEBQ7LlqgyUPowESBX6qEKMMCjpsaJcL48TBxkZERcWjMmDnjBg4ydxAt/NSEhI0YNGnaeHOGyAeCXUDkWBE0KAw6IrII7ISBSZQ9a5rOlHHiyR89nDx54fMjkYY5bbSq8MGAgxxAW66AweNgAZU0b4LGULMBQoUdYpz0GaOkAawHQ0wETSHkwWEgZo5ICTJlQiwCm3ikkCHjhQ4NEWI5KLIkiRYughLEikWhQw8UbOoUkrC6AZJBWBo1clRrdawImqxYUTQg02oDgRg9EihgFyzfBRAgsOAbVoZDC1Xheu67OyxbrS4sJgzlSpes7r5xvSI1ERQqW7lmPZ+V61YqUR8FjjK1ihYsVqeUwlBAADs="};

typedef struct _agent_state
{
	t_symbol		*x_name;	/* agent's name */
	t_int			x_type;
	t_int			x_pos[3];	/* agent's position */
	t_int			x_vel[3];	/* agent's velocity */
	t_int			x_ori[3];	/* agent's orientation */
	struct _agent_state *next;
} agent_state;

typedef struct _audce_gui_grid
{
    t_object	x_obj;
    t_glist		*x_glist;
	t_inlet		**x_inlets;
	t_outlet	**x_outlets;
    t_int		x_num_agents;
	t_int		x_selected_state;	/* stores selected state					*/
    t_int		x_type_selected;	/* type of selected object					*/
	agent_state	*x_agents;			/* linked list with agents					*/
    agent_state *x_selected;		/* selected agent							*/
    t_int		x_height; 			/* height of the 2d_space object            */
    t_int		x_width; 			/* width of the 2d_space object             */
	t_float		x_pixelsize;		/* armazena o valor em metros de 1 pixel	*/
} t_audce_gui_grid;

t_widgetbehavior audce_gui_grid_widgetbehavior;
static t_class *audce_gui_grid_class;

t_float meter[3];
t_int	pixel[3];

/* conversion functions */
/* ord = 0 pixel to meter
/* ord = 1 meter to pixel */
static void audce_gui_grid_convert_pixel_to_meter(t_audce_gui_grid * x, t_int * pixel, t_float * meter)
{
	meter[0] = ((float)-pixel[1] + (float)x->x_height/2) * x->x_pixelsize;
	meter[1] = ((float)-pixel[0] + (float)x->x_width/2) * x->x_pixelsize;
	meter[2] = pixel[2];
}

static void audce_gui_grid_convert_meter_to_pixel(t_audce_gui_grid * x, t_int * pixel, t_float * meter)
{
	pixel[0] = (int)(-(meter[1] / x->x_pixelsize) + (float)x->x_height/2);
	pixel[1] = (int)(-(meter[0] / x->x_pixelsize) + (float)x->x_width/2);
	pixel[2] = meter[2];
}

// envia a msg de mov
// pos_src <num_src> <x> <y> <z> 
// pos_rcv <x> <y> <z> 
// num src pode ser o nome!!!
static void audce_gui_grid_send_pos(t_audce_gui_grid *x, agent_state * sel) {
	t_atom atoms[4];

	audce_gui_grid_convert_pixel_to_meter(x, sel->x_pos, meter);

	// Passa a msg da posição da fonte
	SETSYMBOL(&atoms[0], sel->x_name);
	SETFLOAT(&atoms[1], meter[0]);
	SETFLOAT(&atoms[2], meter[1]);
	SETFLOAT(&atoms[3], meter[2]);
	outlet_anything(x->x_outlets[0], gensym("pos"), 4, &atoms);
}

// envia a msg de mov
static void audce_gui_grid_bang(t_audce_gui_grid *x) {
	
	int i;
	agent_state * ptr;
	
	ptr = x->x_agents;
	while (ptr != NULL) {
		audce_gui_grid_send_pos(x, ptr);
		ptr = ptr->next;
	}

}

/* drawing functions */
static void audce_gui_grid_draw_update(t_audce_gui_grid * x, t_glist * glist) 
{

	t_canvas *canvas=glist_getcanvas(glist);
	t_int ei;
	agent_state * ptr;

	ptr = x->x_agents;
	while (ptr != NULL) {
		sys_vgui(".x%x.c coords %xISPEAKER%x %d %d\n",
				canvas, x, ptr, 
				text_xpix(&x->x_obj, glist) + ptr->x_pos[0],
				text_ypix(&x->x_obj, glist) + ptr->x_pos[1]
				);
		sys_vgui(".x%x.c coords %xSPEAKERNUM%x %d %d\n",
				canvas, x, ptr, 
				text_xpix(&x->x_obj, glist) + ptr->x_pos[0] - SPEAKER_WIDTH, 
				text_ypix(&x->x_obj, glist) + ptr->x_pos[1] - SPEAKER_HEIGHT
				);
		ptr = ptr->next;
	}

}

static void audce_gui_grid_draw_agent(t_audce_gui_grid *x, t_glist *glist, agent_state * agent)
{
	t_canvas *canvas=glist_getcanvas(glist);
	agent_state * ptr;
	int ei = 0;
	
	// Desenha os alto-falantes
	switch (agent->x_type) {
		case AUDCE_GUI_GRID_SRC:
			sys_vgui("image create photo %xSPEAKER%x -data %s -format gif -width %d -height %d\n", 
				x, agent, &src_img, SPEAKER_WIDTH, SPEAKER_HEIGHT);
			break;
		case AUDCE_GUI_GRID_RCV:
			sys_vgui("image create photo %xSPEAKER%x -data %s -format gif -width %d -height %d\n", 
				x, agent, &rcv_img, SPEAKER_WIDTH, SPEAKER_HEIGHT);
			break;
	}

//	sys_vgui(".x%x.c create image %d %d -image %xSPEAKER%d -tags %xISPEAKER%d\n",
	sys_vgui(".x%x.c create image %d %d -image %xSPEAKER%x -tags %xISPEAKER%x\n",
		canvas, 
		text_xpix(&x->x_obj, glist) + agent->x_pos[0],
		text_ypix(&x->x_obj, glist) + agent->x_pos[1],
		x, agent, x, agent);
	sys_vgui(".x%x.c create text %d %d -font -*-courier-bold--normal--12-* -text \"%s\" -tags %xSPEAKERNUM%x\n",
		canvas, 
		text_xpix(&x->x_obj, glist) + agent->x_pos[0] - SPEAKER_WIDTH, 
		text_ypix(&x->x_obj, glist) + agent->x_pos[1] - SPEAKER_HEIGHT, 
		agent->x_name->s_name, x, agent);
	/*
	// Desenha o ouvinte
	sys_vgui("image create photo %xLISTENER -data %s -format gif -width %d -height %d\n", 
             x, &rcv_img, LISTENER_WIDTH, LISTENER_HEIGHT);
	sys_vgui(".x%x.c create image %d %d -image %xLISTENER -tags %xILISTENER\n",
                      canvas, 
                      text_xpix(&x->x_obj, glist) + x->x_rcv[0],
                      text_ypix(&x->x_obj, glist) + x->x_rcv[1],
                      x, x);
	*/

	canvas_fixlinesfor( canvas, (t_text*)x );
}


static void audce_gui_grid_draw_new(t_audce_gui_grid *x, t_glist *glist)
{
	t_canvas *canvas=glist_getcanvas(glist);
	agent_state * ptr;
	int ei;
	
	// Desenha o retangulo
    sys_vgui(".x%x.c create rectangle %d %d %d %d -fill #EAF1E2 -tags %xAUDCE_GUI_GRID\n",
	     canvas, text_xpix(&x->x_obj, glist), text_ypix(&x->x_obj, glist),
	     text_xpix(&x->x_obj, glist) + x->x_width, text_ypix(&x->x_obj, glist) + x->x_height,
	     x);

    // Desenha os outlet
    sys_vgui(".x%x.c create rectangle %d %d %d %d -outline #000000 -fill #000000 -tags %xOUT%d\n",
            canvas, text_xpix(&x->x_obj, glist),
            text_ypix(&x->x_obj, glist) + x->x_height,
            text_xpix(&x->x_obj, glist) + 5,
            text_ypix(&x->x_obj, glist) + x->x_height + 2,
            x, 0);
    
	// Desenha os inlet
    for ( ei=0; ei<1; ei++ )
    {
		sys_vgui(".x%x.c create rectangle %d %d %d %d -outline #000000 -fill #000000 -tags %xIN%d\n",
			canvas, text_xpix(&x->x_obj, glist) + ( ei * (x->x_width - 5) )/ (1),
			text_ypix(&x->x_obj, glist) - 2,
			text_xpix(&x->x_obj, glist) + ( ei * (x->x_width - 5) )/ (1) + 5,
			text_ypix(&x->x_obj, glist),
			x, ei);
    }

	canvas_fixlinesfor( canvas, (t_text*)x );
}

static void audce_gui_grid_draw_move(t_audce_gui_grid *x, t_glist *glist)
{
	t_canvas *canvas=glist_getcanvas(glist);
	t_int ei;
	agent_state * ptr;

	sys_vgui(".x%x.c coords %xAUDCE_GUI_GRID %d %d %d %d\n",
	     canvas, x,
	     text_xpix(&x->x_obj, glist), text_ypix(&x->x_obj, glist),
	     text_xpix(&x->x_obj, glist)+x->x_width, text_ypix(&x->x_obj, glist)+x->x_height);
    
	for ( ei=0; ei<1; ei++ )
    {
         sys_vgui(".x%x.c coords %xIN%d %d %d %d %d\n",
             canvas, x, ei, text_xpix(&x->x_obj, glist) + ( ei * (x->x_width - 5) )/ (1),
             text_ypix(&x->x_obj, glist) - 2,
             text_xpix(&x->x_obj, glist) + ( ei * (x->x_width - 5) )/ (1) + 5,
             text_ypix(&x->x_obj, glist) 
             );
    }
	
	ptr = x->x_agents;
	while (ptr != NULL) {
        sys_vgui(".x%x.c coords %xISPEAKER%x %d %d\n",
			canvas, x, ptr, 
			text_xpix(&x->x_obj, glist) + ptr->x_pos[0],
			text_ypix(&x->x_obj, glist) + ptr->x_pos[1]
			);
        sys_vgui(".x%x.c coords %xSPEAKERNUM%x %d %d\n",
			canvas, x, ptr, 
			text_xpix(&x->x_obj, glist) + ptr->x_pos[0] - SPEAKER_WIDTH, 
			text_ypix(&x->x_obj, glist) + ptr->x_pos[1] - SPEAKER_HEIGHT
			);
		ptr = ptr->next;
    }

	sys_vgui(".x%x.c coords %xOUT%d %d %d %d %d\n",
		canvas, x, 0, text_xpix(&x->x_obj, glist),
		text_ypix(&x->x_obj, glist) + x->x_height,
		text_xpix(&x->x_obj, glist) + 5,
		text_ypix(&x->x_obj, glist) + x->x_height + 2
		);
		 
	canvas_fixlinesfor( canvas, (t_text*)x );

}

//static void audce_gui_grid_draw_reset(t_audce_gui_grid* x, 

static void audce_gui_grid_draw_erase(t_audce_gui_grid* x,t_glist* glist)
{

	t_canvas *canvas=glist_getcanvas(glist);
	int ei;
	agent_state * ptr;

    sys_vgui(".x%x.c delete %xAUDCE_GUI_GRID\n", canvas, x);
	for ( ei=0; ei<x->x_inlets+1; ei++ )
    {
		sys_vgui(".x%x.c delete %xIN%d\n", canvas, x, ei );
		sys_vgui(".x%x.c delete %xOUT%d\n", canvas, x, ei );
    }
	ptr = x->x_agents;
	while (ptr != NULL) {
        sys_vgui(".x%x.c delete %xISPEAKER%x\n", canvas, x, ptr);
        sys_vgui(".x%x.c delete %xSPEAKERNUM%x\n", canvas, x, ptr);
        sys_vgui("image delete %xSPEAKER%x\n", x, ptr);
		ptr = ptr->next;
    }
}

static void audce_gui_grid_draw_select(t_audce_gui_grid* x,t_glist* glist)
{
    t_canvas *canvas=glist_getcanvas(glist);

	if(x->x_selected_state != NULL)
    {
        /* sets the item in blue */
		sys_vgui(".x%x.c itemconfigure %xAUDCE_GUI_GRID -outline #0000FF\n", canvas, x);
    }
    else
    {
		sys_vgui(".x%x.c itemconfigure %xAUDCE_GUI_GRID -outline #000000\n", canvas, x);
    }
}

/* ------------------------ audce_gui_grid widgetbehaviour----------------------------- */
static void audce_gui_grid_getrect(t_gobj *z, t_glist *owner,
			    int *xp1, int *yp1, int *xp2, int *yp2)
{
	t_audce_gui_grid* x = (t_audce_gui_grid*)z;

	*xp1 = text_xpix(&x->x_obj, owner);
	*yp1 = text_ypix(&x->x_obj, owner);
	*xp2 = text_xpix(&x->x_obj, owner)+x->x_width;
	*yp2 = text_ypix(&x->x_obj, owner)+x->x_height;
}

static void audce_gui_grid_properties(t_gobj *z, t_glist *owner)
{
	char buf[800];
	t_audce_gui_grid *x=(t_audce_gui_grid *)z;

	sprintf(buf, "pdtk_audce_gui_grid_dialog %%s %d %d %d\n",
			x->x_width, x->x_height, x->x_num_agents
			);
	// post("audce_gui_grid_properties : %s", buf );
	gfxstub_new(&x->x_obj.ob_pd, x, buf);
}

static void audce_gui_grid_select(t_gobj *z, t_glist *glist, int selected)
{
	t_audce_gui_grid *x = (t_audce_gui_grid *)z;

	x->x_selected_state = selected;
	audce_gui_grid_draw_select( x, glist );
}

static void audce_gui_grid_vis(t_gobj *z, t_glist *glist, int vis)
{
	t_audce_gui_grid *x = (t_audce_gui_grid *)z;

	// post( "audce_gui_grid : vis : %d", vis );
	if (vis)
	{
		audce_gui_grid_draw_new( x, glist );
	}
	else
	{
		audce_gui_grid_draw_erase( x, glist );
	}
}

static void audce_gui_grid_dialog(t_audce_gui_grid *x, t_symbol *s, int argc, t_atom *argv)
{
  t_int onumsrc = x->x_num_agents;
  t_int owidth = x->x_width;
  t_int oheight = x->x_height;
  t_int i;
  t_canvas *canvas=glist_getcanvas(x->x_glist);

   if ( !x ) {
     post( "audce_gui_grid : error :tried to set properties on an unexisting object" );
   }
   if ( argc != 3 )
   {
      post( "audce_gui_grid : error in the number of arguments ( %d instead of 3 )", argc );
      return;
   }
      if ( argv[0].a_type != A_FLOAT || argv[1].a_type != A_FLOAT ||
           argv[2].a_type != A_FLOAT ) {
      post( "audce_gui_grid : wrong arguments" );
      return;
   }

   audce_gui_grid_draw_erase(x, x->x_glist);

   x->x_width = (int)argv[0].a_w.w_float;
   if ( x->x_width < 10 ) x->x_width = 10;
   x->x_height = (int)argv[1].a_w.w_float;
   if ( x->x_height < 10 ) x->x_height = 10;
   x->x_num_agents = (int)argv[2].a_w.w_float;
   if ( x->x_num_agents < 1 ) x->x_num_agents = 1;

   // re-allocate inlets : CRASHES PD,I GUESS IT'S NOT SUPPORTED
   if ( onumsrc != x->x_num_agents )
   {
     // post( "audce_gui_grid : cleaning up old inlets" );
     if (x->x_agents) {
		 for(i=0;i<onumsrc;i++) {
			 freebytes(&x->x_agents[i], sizeof(agent_state));
		 }
	 }
     // post( "audce_gui_grid : creating new ones" );

     if (!x->x_agents)
     {
       error( "audce_gui_grid : fatal : could not create new object" );
       return;
     }
   }

	canvas_fixlinesfor( canvas, (t_text*)x );
	audce_gui_grid_draw_new(x, x->x_glist);
}

static void audce_gui_grid_delete(t_gobj *z, t_glist *glist)
{
	t_audce_gui_grid *x = (t_audce_gui_grid *)z;

    audce_gui_grid_draw_erase( x, glist );
    canvas_deletelinesfor( glist_getcanvas(glist), (t_text *)z);
}

static void audce_gui_grid_displace(t_gobj *z, t_glist *glist, int dx, int dy)
{
  t_audce_gui_grid *x = (t_audce_gui_grid *)z;
  int xold = text_xpix(&x->x_obj, glist);
  int yold = text_ypix(&x->x_obj, glist);

    x->x_obj.te_xpix += dx;
    x->x_obj.te_ypix += dy;
    if(xold != x->x_obj.te_xpix || yold != x->x_obj.te_ypix)
    {
	audce_gui_grid_draw_move(x, x->x_glist);
    }
}

static void audce_gui_grid_motion(t_audce_gui_grid *x, t_floatarg dx, t_floatarg dy)
{

	switch( x->x_type_selected )
    {
       case AUDCE_GUI_GRID_SRC:
		   x->x_selected->x_pos[0] += dx;
			if(x->x_selected->x_pos[0] < 0) 
				x->x_selected->x_pos[0] = 0;
			if(x->x_selected->x_pos[0] > x->x_width ) 
				x->x_selected->x_pos[0] = x->x_width;
			
			x->x_selected->x_pos[1] += dy;
			if(x->x_selected->x_pos[1] < 0) 
				x->x_selected->x_pos[1] = 0;
			if(x->x_selected->x_pos[1] > x->x_height ) 
				x->x_selected->x_pos[1] = x->x_height;

			audce_gui_grid_send_pos(x, x->x_selected);
			break;
    }

    audce_gui_grid_draw_update(x, x->x_glist);
}

static int audce_gui_grid_click(t_gobj *z, struct _glist *glist,
			    int xpix, int ypix, int shift, int alt, int dbl, int doit)
{
    t_audce_gui_grid* x = (t_audce_gui_grid *)z;
    t_int bi;
	agent_state * ptr;

	//post( "audce_gui_grid_click doit=%d x=%d y=%d", doit, xpix, ypix );
	if (doit) 
	{
		t_int relx = xpix-text_xpix(&x->x_obj, glist);
		t_int rely = ypix-text_ypix(&x->x_obj, glist);

		  //post( "audce_gui_grid : relx : %d : rely : %d", relx, rely );
		  x->x_type_selected = AUDCE_GUI_GRID_NONE;
		  x->x_selected = NULL;
		  ptr = x->x_agents;
		  while (ptr != NULL)
		  {

			  if ( ( abs( relx - ptr->x_pos[0]) < SPEAKER_WIDTH ) && 
				   ( abs( rely - ptr->x_pos[1]) < SPEAKER_HEIGHT ) ) { 
				 x->x_type_selected = AUDCE_GUI_GRID_SRC;
				 x->x_selected = ptr;
				 break;
			  }
			  ptr = ptr->next;
		  }
		  audce_gui_grid_draw_update(x, glist);
		  glist_grab(glist, &x->x_obj.te_g, (t_glistmotionfn)audce_gui_grid_motion, 0, xpix, ypix);
	}

	return (1);

}

// parametros: nome_fonte x y z
static void audce_gui_grid_agent(t_audce_gui_grid *x, t_symbol *s, int argc, t_atom *argv) {

	int		i, sel;
	char	name[MAXPDSTRING];
	char	type[MAXPDSTRING];
	agent_state * ptr;
	agent_state * last_ptr;

	if ( argc != 5) {
		post( "audce_gui_grid: agent - número de argumentos errado %d - (agent_name agent_type x y z)", argc);
		return;
	}

	if(	argv[0].a_type != A_SYMBOL || argv[1].a_type != A_SYMBOL || 
		argv[2].a_type != A_FLOAT || argv[3].a_type != A_FLOAT || argv[4].a_type != A_FLOAT) {
		post( "audce_gui_grid: agent - argumentos não numéricos" );
		return;
	}

	// Obtém os parâmetros do comando
	atom_string(argv, name, MAXPDSTRING);
	atom_string(argv+1, type, MAXPDSTRING);
	meter[0] = atom_getfloatarg(2, argc, argv);
	meter[1] = atom_getfloatarg(3, argc, argv);
	meter[2] = atom_getfloatarg(4, argc, argv);

	// Verifca se já existe o agente
	ptr = x->x_agents;
	last_ptr = x->x_agents;
	while(ptr != NULL) {
		if (strcmp(name, ptr->x_name->s_name) == 0) {
			// post("Agente já existe");
			break;
		}
		last_ptr = ptr;
		ptr = ptr->next;
	}

	// Se não existe
	if (ptr == NULL) {
		// cria novo agente
		agent_state * new_agent = (agent_state *) getbytes(sizeof(agent_state));
		new_agent->x_name = gensym(name);
		if (strcmp(type,"src") == 0) {
			new_agent->x_type = AUDCE_GUI_GRID_SRC;
		} 
		else if (strcmp(type,"rcv") == 0) {
			new_agent->x_type = AUDCE_GUI_GRID_RCV;
		}
		else {
			freebytes(new_agent, sizeof(agent_state));
			post("audce_gui_grid: agent - type não existe!");
			return;
		}
		new_agent->next = NULL;
		audce_gui_grid_convert_meter_to_pixel(x, new_agent->x_pos, meter);
		// insere na lista
		if (last_ptr != NULL) {
			last_ptr->next = new_agent;
		} 
		else {
			x->x_agents = new_agent;
		}
		x->x_num_agents++;
		audce_gui_grid_draw_agent(x, x->x_glist, new_agent);
		post("audce_gui_grid: pos_rcv - Nova fonte criada!");
	}
	// Se existe, atualiza a posição
	else {
		post( "audce_gui_grid: agente já existe");
	}

    audce_gui_grid_draw_update(x, x->x_glist);
	//audce_gui_grid_send_pos(x, sel);

}

/*
// parametros: nome_fonte x y z
static void audce_gui_grid_vel(t_audce_gui_grid *x, t_symbol *s, int argc, t_atom *argv) {
	t_int sel;

	if ( argc != 4) {
			post( "audce_gui_grid: pos_src - número de argumentos errado!");
			return;
	}

	if ( argv[0].a_type != A_FLOAT || argv[1].a_type != A_FLOAT || argv[2].a_type != A_FLOAT || argv[3].a_type != A_FLOAT) {
			post( "audce_gui_grid: pos_src - argumentos não numéricos" );
			return;
	}

	sel = (int)atom_getfloatarg(0, argc, argv) - 1;
	if ( sel < 0 || sel >= x->x_num_agents) {
			post( "audce_gui_grid: pos_rcv - Fonte inexistente!");
			return;
	}

	meter[0] = atom_getfloatarg(1, argc, argv);
	meter[1] = atom_getfloatarg(2, argc, argv);
	meter[2] = atom_getfloatarg(3, argc, argv);

	audce_gui_grid_draw_update(x, x->x_glist);
	audce_gui_grid_send_pos(x, sel);
}
*/

// parametros: nome_fonte x y z
static void audce_gui_grid_pos(t_audce_gui_grid *x, t_symbol *s, int argc, t_atom *argv) {

	int		i, sel;
	char	name[MAXPDSTRING];
	agent_state * ptr;
	agent_state * last_ptr;

	if ( argc != 4) {
		post( "audce_gui_grid: pos - número de argumentos errado - (agent_name agent_type x y z)");
		return;
	}

	if(	argv[0].a_type != A_SYMBOL || argv[1].a_type != A_FLOAT || argv[2].a_type != A_FLOAT || argv[3].a_type != A_FLOAT) {
		post( "audce_gui_grid: pos - argumentos não numéricos" );
		return;
	}

	// Obtém os parâmetros do comando
	atom_string(argv, name, MAXPDSTRING);
	meter[0] = atom_getfloatarg(1, argc, argv);
	meter[1] = atom_getfloatarg(2, argc, argv);
	meter[2] = atom_getfloatarg(3, argc, argv);

	// Verifca se já existe o agente
	ptr = x->x_agents;
	last_ptr = x->x_agents;
	while(ptr != NULL) {
		if (strcmp(name, ptr->x_name->s_name) == 0) {
			break;
		}
		last_ptr = ptr;
		ptr = ptr->next;
	}

	// Se não existe
	if (ptr == NULL) {
		post( "audce_gui_grid: pos - agente não existe!");
	}
	// Se existe, atualiza a posição
	else {
		audce_gui_grid_convert_meter_to_pixel(x, ptr->x_pos, meter);
	}

    audce_gui_grid_draw_update(x, x->x_glist);
	//audce_gui_grid_send_pos(x, sel);

}

/*
// parametros: nome_fonte x y z
static void audce_gui_grid_vel(t_audce_gui_grid *x, t_symbol *s, int argc, t_atom *argv) {
	t_int sel;

	if ( argc != 4) {
			post( "audce_gui_grid: pos_src - número de argumentos errado!");
			return;
	}

	if ( argv[0].a_type != A_FLOAT || argv[1].a_type != A_FLOAT || argv[2].a_type != A_FLOAT || argv[3].a_type != A_FLOAT) {
			post( "audce_gui_grid: pos_src - argumentos não numéricos" );
			return;
	}

	sel = (int)atom_getfloatarg(0, argc, argv) - 1;
	if ( sel < 0 || sel >= x->x_num_agents) {
			post( "audce_gui_grid: pos_rcv - Fonte inexistente!");
			return;
	}

	meter[0] = atom_getfloatarg(1, argc, argv);
	meter[1] = atom_getfloatarg(2, argc, argv);
	meter[2] = atom_getfloatarg(3, argc, argv);

	audce_gui_grid_draw_update(x, x->x_glist);
	audce_gui_grid_send_pos(x, sel);
}
*/

static t_audce_gui_grid *audce_gui_grid_new(t_symbol *s, int argc, t_atom *argv)
{
    t_audce_gui_grid *x;
	t_int i, j;
    t_int bi, ei;
 
    post( "audce_gui_grid_new : create : %s argc =%d", s->s_name, argc );

    x = (t_audce_gui_grid *)pd_new(audce_gui_grid_class);
    // new audce_gui_grid created from the gui 
    if ( argc != 0 )
    {
		if ( argc != 3)
		{
			post( "audce_gui_grid : erro no número de argumentos (largura, altura, pixel_ratio)", argc );
			return NULL;
		}
		if ( argv[0].a_type != A_FLOAT || argv[1].a_type != A_FLOAT || argv[2].a_type != A_FLOAT) {
			post( "audce_gui_grid : argumentos não numéricos" );
			return NULL;
		}

		x->x_width = (int)argv[0].a_w.w_float;
		if ( x->x_width < 10 ) 
			x->x_width = DEFAULT_WIDTH;
		x->x_height = (int)argv[1].a_w.w_float;
		if ( x->x_height < 10 ) 
			x->x_height = DEFAULT_HEIGHT;
		x->x_pixelsize = argv[2].a_w.w_float;
	}
    else
    {
		x->x_width = DEFAULT_WIDTH;
		x->x_height = DEFAULT_HEIGHT;
		x->x_pixelsize = DEFAULT_PIXELSIZE;
    }

    // create inlets and outlets
	x->x_outlets = (t_outlet **) getbytes(sizeof(t_outlet *));
    x->x_outlets[0] = outlet_new(&x->x_obj, &s_anything); 

	// create agents state
	x->x_agents = NULL;
	x->x_num_agents = 0;
	/*
	x->x_agents = (agent_state *)getbytes(x->x_num_agents*sizeof(agent_state *));
	for(i = 0; i < x->x_num_agents; i++) {
		x->x_agents[i].x_name.s_name = "teste";
		meter[0] = 0.0f; meter[1] = 0.0f; meter[2] = 0.0f;
		audce_gui_grid_convert_meter_to_pixel(x, x->x_agents[i].x_pos, meter);
		x->x_agents[i].x_vel[0] = 0; x->x_agents[i].x_vel[1] = 0; x->x_agents[i].x_vel[2] = 0;
		x->x_agents[i].x_ori[0] = 0; x->x_agents[i].x_ori[1] = 0; x->x_agents[i].x_ori[2] = 0;
	}
	*/

    if (!x->x_outlets)
    {
       post( "audce_gui_grid : erro ao alocar memória" );
       return NULL;
    }

	/*
    if ( argc <= 4 )
    {
		// set default coordinates
		for ( ei=0; ei<x->x_num_agents; ei++ )
		{
			x->x_agents[ei].x_pos[0] = (ei+1) * (x->x_width - 5) / x->x_num_agents;
			x->x_agents[ei].x_pos[1] = SPEAKER_HEIGHT/2;
			x->x_agents[ei].x_pos[2] = 0;
		}
    }
    else
    {
		t_int ai = 4;

		// restore coordinates from arguments
		for ( ei=0; ei<x->x_num_agents; ei++ )
		{
			meter[0] = argv[ai++].a_w.w_float;
			meter[1] = argv[ai++].a_w.w_float;
			meter[2] = argv[ai++].a_w.w_float;
			audce_gui_grid_convert_meter_to_pixel(x, x->x_agents[ei].x_pos, meter);
		}
	}
	*/

    x->x_glist = (t_glist *) canvas_getcurrent();
    x->x_type_selected = AUDCE_GUI_GRID_NONE;
    x->x_selected = NULL;

    // post( "audce_gui_grid : new object : inlets : %d : outlets : %d : attenuation : %f", x->x_num_agents, x->x_nboutputs, x->x_attenuation );
    return (x);
}

static void audce_gui_grid_free(t_audce_gui_grid *x)
{
	freebytes(x->x_outlets, x->x_num_agents*sizeof(t_outlet *));
	//freebytes(x->x_agents, x->x_num_agents*sizeof(agent_state *));
}

void audce_gui_grid_setupgui(void) {
	// ########### aud_gui_grid procedures -- ydegoyon@free.fr #########
	sys_gui("proc aud_gui_grid_apply {id} {\n");
	sys_gui("set vid [string trimleft $id .]\n");
	// for each variable, make a local variable to hold its name...
	sys_gui("set var_graph_width [concat graph_width_$vid]\n");
	sys_gui("global $var_graph_width\n");
	sys_gui("set var_graph_height [concat graph_height_$vid]\n");
	sys_gui("global $var_graph_height\n");
	sys_gui("set var_graph_nboutputs [concat graph_nboutputs_$vid]\n");
	sys_gui("global $var_graph_nboutputs\n");
	sys_gui("set cmd [concat $id dialog [eval concat $$var_graph_width] [eval concat $$var_graph_height] [eval concat $$var_graph_nboutputs] \\;]\n");
	// puts stderr $cmd
	sys_gui("pd $cmd\n");
	sys_gui("}\n");
	sys_gui("proc aud_gui_grid_cancel {id} {\n");
	sys_gui("set cmd [concat $id cancel \\;]\n");
	// puts stderr $cmd
	sys_gui("pd $cmd\n");
	sys_gui("}\n");
	sys_gui("proc aud_gui_grid_ok {id} {\n");
	sys_gui("aud_gui_grid_apply $id\n");
	sys_gui("aud_gui_grid_cancel $id\n");
	sys_gui("}\n");
	sys_gui("proc pdtk_aud_gui_grid_dialog {id width height send-symbol receive-symbol} {\n");
	sys_gui("set vid [string trimleft $id .]\n");
	sys_gui("set var_graph_width [concat graph_width_$vid]\n");
	sys_gui("global $var_graph_width\n");
	sys_gui("set var_graph_height [concat graph_height_$vid]\n");
	sys_gui("global $var_graph_height\n");
	sys_gui("set var_graph_nboutputs [concat graph_nboutputs_$vid]\n");
	sys_gui("global $var_graph_nboutputs\n");
	sys_gui("set $var_graph_width $width\n");
	sys_gui("set $var_graph_height $height\n");
	sys_gui("set $var_graph_nboutputs $nboutputs\n");
	sys_gui("toplevel $id\n");
	sys_gui("wm title $id {aud_gui_grid}\n");
	sys_gui("wm protocol $id WM_DELETE_WINDOW [concat aud_gui_grid_cancel $id]\n");
	sys_gui("label $id.label -text {2$ SPACE PROPERTIES}\n");
	sys_gui("pack $id.label -side top\n");
	sys_gui("frame $id.buttonframe\n");
	sys_gui("pack $id.buttonframe -side bottom -fill x -pady 2m\n");
	sys_gui("button $id.buttonframe.cancel -text {Cancel} -command \"aud_gui_grid_cancel $id\"\n");
	sys_gui("button $id.buttonframe.apply -text {Apply} -command \"aud_gui_grid_apply $id\"\n");
	sys_gui("button $id.buttonframe.ok -text {OK} -command \"aud_gui_grid_ok $id\"\n");
	sys_gui("pack $id.buttonframe.cancel -side left -expand 1\n");
	sys_gui("pack $id.buttonframe.apply -side left -expand 1\n");
	sys_gui("pack $id.buttonframe.ok -side left -expand 1\n");
	sys_gui("frame $id.1rangef\n");
	sys_gui("pack $id.1rangef -side top\n");
	sys_gui("label $id.1rangef.lwidth -text \"Width :\"\n");
	sys_gui("entry $id.1rangef.width -textvariable $var_graph_width -width 7\n");
	sys_gui("pack $id.1rangef.lwidth $id.1rangef.width -side left\n");
	sys_gui("frame $id.2rangef\n");
	sys_gui("pack $id.2rangef -side top\n");
	sys_gui("label $id.2rangef.lheight -text \"Height :\"\n");
	sys_gui("entry $id.2rangef.height -textvariable $var_graph_height -width 7\n");
	sys_gui("pack $id.2rangef.lheight $id.2rangef.height -side left\n");
	sys_gui("frame $id.3rangef\n");
	sys_gui("pack $id.3rangef -side top\n");
	sys_gui("label $id.3rangef.lnboutputs -text \"Nb Listeners :\"\n");
	sys_gui("entry $id.3rangef.nboutputs -textvariable $var_graph_nboutputs -width 7\n");
	sys_gui("pack $id.3rangef.lnboutputs $id.3rangef.nboutputs -side left\n");
	sys_gui("bind $id.1rangef.width <KeyPress-Return> [concat aud_gui_grid_ok $id]\n");
	sys_gui("bind $id.2rangef.height <KeyPress-Return> [concat aud_gui_grid_ok $id]\n");
	sys_gui("bind $id.3rangef.nboutputs <KeyPress-Return> [concat aud_gui_grid_ok $id]\n");
	sys_gui("focus $id.1rangef.width\n");
	sys_gui("}\n");

}

void audce_gui_grid_setup(void)
{
	audce_gui_grid_setupgui();
	audce_gui_grid_class = class_new(gensym("audce_gui_grid"), (t_newmethod)audce_gui_grid_new,
			      (t_method)audce_gui_grid_free, sizeof(t_audce_gui_grid), CLASS_DEFAULT, A_GIMME, 0);
    class_addmethod(audce_gui_grid_class, (t_method)audce_gui_grid_dialog, gensym("dialog"), A_GIMME, 0);
	//class_addmethod(audce_gui_grid_class, (t_method)audce_gui_grid_send, gensym("send"), A_DEFSYM, 0);
	//class_addmethod(audce_gui_grid_class, (t_method)audce_gui_grid_receive, gensym("receive"), A_DEFSYM, 0);
    
	audce_gui_grid_widgetbehavior.w_getrectfn =    audce_gui_grid_getrect;
    audce_gui_grid_widgetbehavior.w_displacefn =   audce_gui_grid_displace;
    audce_gui_grid_widgetbehavior.w_selectfn =     audce_gui_grid_select;
    audce_gui_grid_widgetbehavior.w_activatefn =   NULL;
    audce_gui_grid_widgetbehavior.w_deletefn =     audce_gui_grid_delete;
    audce_gui_grid_widgetbehavior.w_visfn =        audce_gui_grid_vis;
    audce_gui_grid_widgetbehavior.w_clickfn =      audce_gui_grid_click;

	class_setpropertiesfn(audce_gui_grid_class, audce_gui_grid_properties);
    //class_setsavefn(audce_gui_grid_class, audce_gui_grid_save);
    class_setwidget(audce_gui_grid_class, &audce_gui_grid_widgetbehavior);
    //class_sethelpsymbol(audce_gui_grid_class, gensym("audce_gui_grid.pd"));

	// Métodos que obtem os parametros de entrada
	class_addmethod(audce_gui_grid_class, (t_method)audce_gui_grid_agent, gensym("agent"), A_GIMME, 0);
	class_addmethod(audce_gui_grid_class, (t_method)audce_gui_grid_pos, gensym("pos"), A_GIMME, 0);
	//class_addmethod(audce_gui_grid_class, (t_method)audce_gui_grid_vel, gensym("vel"), A_GIMME, 0);

	// Método que exibe todos os parâmetros
    class_addbang(audce_gui_grid_class, (t_method)audce_gui_grid_bang);
	
	// MŽtodo para limpar o grid
    //class_addmethod(audce_gui_grid_class, (t_method)audce_gui_grid_reset, gensym("reset"), A_GIMME, 0);

}
